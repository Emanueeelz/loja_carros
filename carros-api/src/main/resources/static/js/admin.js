const tokenAdmin = sessionStorage.getItem("garagem_token");

const painel = document.getElementById("painel-admin");
const aviso = document.getElementById("aviso-acesso");
const mensagem = document.getElementById("mensagem-admin");
const formulario = document.getElementById("form-carro");
const lista = document.getElementById("lista-admin");

let carrosAdmin = [];
let idEmEdicao = null;

// Verifica o perfil informado no token.
// A autorização real continua sendo feita pelo backend.
function possuiPerfilAdmin(token) {
    if (!token) return false;

    try {
        const parte = token.split(".")[1]
            .replace(/-/g, "+")
            .replace(/_/g, "/");

        const dados = JSON.parse(atob(parte));

        return dados.perfil === "ADMIN" &&
            dados.exp > Date.now() / 1000;

    } catch (erro) {
        return false;
    }
}

// Mensagens do painel
function mostrarMensagemAdmin(texto, tipo = "erro") {
    mensagem.textContent = texto;
    mensagem.className = `mensagem-admin ${tipo}`;
}

// Requisições autenticadas
async function requisitar(url, opcoes = {}) {
    const resposta = await fetch(url, {
        ...opcoes,
        headers: {
            Authorization: `Bearer ${tokenAdmin}`,
            ...opcoes.headers
        }
    });

    if (resposta.status === 401) {
        sessionStorage.removeItem("garagem_token");
        throw new Error("Sessão expirada. Faça login novamente.");
    }

    if (resposta.status === 403) {
        throw new Error("Sua conta não tem permissão de administrador.");
    }

    if (resposta.status === 400) {
        throw new Error("Verifique os dados e a fotografia informados.");
    }

    if (!resposta.ok) {
        throw new Error(`Não foi possível concluir a operação (${resposta.status}).`);
    }

    return resposta.status === 204
        ? null
        : resposta.json();
}

// Obtém os dados do formulário
function obterDadosFormulario() {
    const campos = formulario.elements;

    return {
        marca: campos.marca.value.trim(),
        modelo: campos.modelo.value.trim(),
        anoFabricacao: Number(campos.anoFabricacao.value),
        anoModelo: Number(campos.anoModelo.value),
        cor: campos.cor.value.trim(),
        quilometragem: Number(campos.quilometragem.value),
        combustivel: campos.combustivel.value.trim(),
        transmissao: campos.transmissao.value.trim(),
        preco: campos.preco.value,
        descricao: campos.descricao.value.trim(),
        situacao: campos.situacao.value
    };
}

// Retorna o formulário ao modo de cadastro
function limparFormulario() {
    formulario.reset();
    idEmEdicao = null;

    formulario.elements.foto.required = true;

    document.getElementById("titulo-form").textContent =
        "Cadastrar veículo";

    document.getElementById("salvar-carro").textContent =
        "Cadastrar veículo";

    document.getElementById("cancelar-edicao").hidden = true;
}

// Preenche o formulário para editar um veículo
function editarCarro(carro) {
    idEmEdicao = carro.id;

    const campos = formulario.elements;

    campos.marca.value = carro.marca;
    campos.modelo.value = carro.modelo;
    campos.anoFabricacao.value = carro.anoFabricacao;
    campos.anoModelo.value = carro.anoModelo;
    campos.cor.value = carro.cor;
    campos.quilometragem.value = carro.quilometragem;
    campos.combustivel.value = carro.combustivel;
    campos.transmissao.value = carro.transmissao;
    campos.preco.value = carro.preco;
    campos.descricao.value = carro.descricao;
    campos.situacao.value = carro.situacao;

    campos.foto.value = "";
    campos.foto.required = false;

    document.getElementById("titulo-form").textContent =
        `Editar veículo #${carro.id}`;

    document.getElementById("salvar-carro").textContent =
        "Salvar alterações";

    document.getElementById("cancelar-edicao").hidden = false;

    formulario.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

// Exibe os veículos cadastrados
function mostrarVeiculos() {
    lista.innerHTML = "";

    if (carrosAdmin.length === 0) {
        lista.textContent = "Nenhum veículo cadastrado.";
        return;
    }

    carrosAdmin.forEach(carro => {
        const item = document.createElement("article");
        item.className = "admin-veiculo";

        const titulo = document.createElement("h3");
        titulo.textContent = `${carro.marca} ${carro.modelo}`;

        const informacoes = document.createElement("p");
        informacoes.textContent =
            `#${carro.id} • ${carro.anoModelo} • ${carro.situacao}`;

        const preco = document.createElement("strong");
        preco.textContent = Number(carro.preco).toLocaleString(
            "pt-BR",
            { style: "currency", currency: "BRL" }
        );

        const acoes = document.createElement("div");
        acoes.className = "admin-veiculo-acoes";

        const editar = document.createElement("button");
        editar.type = "button";
        editar.className = "botao-secundario";
        editar.textContent = "Editar";
        editar.addEventListener("click", () => editarCarro(carro));

        const excluir = document.createElement("button");
        excluir.type = "button";
        excluir.className = "botao-excluir";
        excluir.textContent = "Desativar";
        excluir.disabled = carro.situacao === "INATIVO";

        excluir.addEventListener("click", () => {
            desativarCarro(carro);
        });

        acoes.append(editar, excluir);
        item.append(titulo, informacoes, preco, acoes);
        lista.appendChild(item);
    });
}

// Consulta todos os veículos
async function carregarVeiculosAdmin() {
    lista.textContent = "Carregando veículos...";

    try {
        carrosAdmin = await requisitar("/adm/carros");
        carrosAdmin.sort((a, b) => b.id - a.id);

        mostrarVeiculos();

    } catch (erro) {
        lista.textContent = "Não foi possível carregar os veículos.";
        mostrarMensagemAdmin(erro.message);
    }
}

// Cadastra ou atualiza um veículo
formulario.addEventListener("submit", async evento => {
    evento.preventDefault();

    const dados = obterDadosFormulario();
    const foto = formulario.elements.foto.files[0];
    const botao = document.getElementById("salvar-carro");

    if (!idEmEdicao && !foto) {
        mostrarMensagemAdmin("Selecione uma fotografia para cadastrar.");
        return;
    }

    if (foto && foto.size > 5 * 1024 * 1024) {
        mostrarMensagemAdmin("A fotografia deve ter até 5 MB.");
        return;
    }

    const editando = idEmEdicao !== null;
    let informacoesSalvas = false;

    botao.disabled = true;
    mostrarMensagemAdmin("");

    try {
        if (!editando) {
            // Cadastro: dados JSON + fotografia
            const envio = new FormData();

            envio.append(
                "dados",
                new Blob(
                    [JSON.stringify(dados)],
                    { type: "application/json" }
                )
            );

            envio.append("foto", foto);

            await requisitar("/carros", {
                method: "POST",
                body: envio
            });

        } else {
            // Atualização das informações
            await requisitar(`/carros/${idEmEdicao}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(dados)
            });

            informacoesSalvas = true;

            // A fotografia só é alterada se uma nova for escolhida
            if (foto) {
                const envioFoto = new FormData();
                envioFoto.append("foto", foto);

                await requisitar(`/carros/${idEmEdicao}/foto`, {
                    method: "PUT",
                    body: envioFoto
                });
            }
        }

        limparFormulario();
        await carregarVeiculosAdmin();

        mostrarMensagemAdmin(
            editando
                ? "Veículo atualizado com sucesso!"
                : "Veículo cadastrado com sucesso!",
            "sucesso"
        );

    } catch (erro) {
        const texto = informacoesSalvas
            ? `Os dados foram salvos, mas a fotografia não foi atualizada. ${erro.message}`
            : erro.message;

        mostrarMensagemAdmin(texto);

        console.error(erro);

    } finally {
        botao.disabled = false;
    }
});

// Desativa um veículo sem removê-lo do banco
async function desativarCarro(carro) {
    const confirmar = confirm(
        `Deseja desativar ${carro.marca} ${carro.modelo}?`
    );

    if (!confirmar) return;

    try {
        await requisitar(`/carros/${carro.id}`, {
            method: "DELETE"
        });

        await carregarVeiculosAdmin();

        if (idEmEdicao === carro.id) {
            limparFormulario();
        }

        mostrarMensagemAdmin(
            "Veículo desativado com sucesso!",
            "sucesso"
        );

    } catch (erro) {
        mostrarMensagemAdmin(erro.message);
    }
}

// Botões auxiliares
document.getElementById("cancelar-edicao")
    .addEventListener("click", limparFormulario);

document.getElementById("atualizar-lista")
    .addEventListener("click", carregarVeiculosAdmin);

document.getElementById("botao-sair")
    .addEventListener("click", () => {
        sessionStorage.removeItem("garagem_token");
        window.location.href = "/login.html";
    });

// Inicialização
if (!possuiPerfilAdmin(tokenAdmin)) {
    aviso.hidden = false;
    aviso.textContent =
        "Acesso restrito. Entre com uma conta de administrador.";

} else {
    aviso.hidden = true;
    painel.hidden = false;

    carregarVeiculosAdmin();
}
