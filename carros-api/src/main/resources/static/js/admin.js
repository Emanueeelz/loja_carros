const tokenAdmin = sessionStorage.getItem("garagem_token");
const painel = document.getElementById("painel-admin");
const aviso = document.getElementById("aviso-acesso");
const mensagem = document.getElementById("mensagem-admin");
const mensagemForm = document.getElementById("mensagem-form");
const lista = document.getElementById("lista-admin");
const formulario = document.getElementById("form-carro");
const modal = document.getElementById("modal-carro");
const salvar = document.getElementById("salvar-carro");
const miniaturas = document.getElementById("lista-fotos-modal");

let carrosAdmin = [];
let idEmEdicao = null;
let galeria = [];
let galeriaAlterada = false;
let versaoModal = 0;
const capasCards = new Set();

function possuiPerfilAdmin(token) {
    if (!token) return false;
    try {
        const dados = JSON.parse(atob(token.split(".")[1]
            .replace(/-/g, "+").replace(/_/g, "/")));
        return dados.perfil === "ADMIN" && dados.exp > Date.now() / 1000;
    } catch {
        return false;
    }
}

function mostrarMensagem(texto, tipo = "erro", destino = mensagem) {
    destino.textContent = texto;
    destino.className = `mensagem-admin ${tipo}`;
}

async function requisitar(url, opcoes = {}) {
    const resposta = await fetch(url, {
        ...opcoes,
        headers: { Authorization: `Bearer ${tokenAdmin}`, ...opcoes.headers }
    });
    if (resposta.status === 401) {
        sessionStorage.removeItem("garagem_token");
        throw new Error("Sessão expirada. Entre novamente.");
    }
    if (resposta.status === 403) {
        throw new Error("Sua conta não tem permissão de administrador.");
    }
    if (resposta.status === 413) {
        throw new Error("Fotografias grandes demais. Máximo de 5 MB por foto.");
    }
    if (resposta.status === 400) {
        throw new Error("Confira os dados e a quantidade de fotografias.");
    }
    if (!resposta.ok) {
        throw new Error(`Não foi possível concluir a operação (${resposta.status}).`);
    }
    if (resposta.status === 204) return null;
    return resposta.json();
}

// A rota /adm/ permite consultar as fotos também de carros inativos.
async function carregarArquivoFoto(id, indice) {
    const resposta = await fetch(`/adm/carros/${id}/fotos/${indice}`, {
        headers: { Authorization: `Bearer ${tokenAdmin}` }
    });
    if (!resposta.ok) throw new Error("Não foi possível carregar uma fotografia.");
    const blob = await resposta.blob();
    const tipo = blob.type || "image/jpeg";
    const extensao = tipo.includes("png") ? "png" : tipo.includes("webp") ? "webp" : "jpg";
    return new File([blob], `foto-${indice}.${extensao}`, { type: tipo });
}

function liberarCapas() {
    capasCards.forEach(url => URL.revokeObjectURL(url));
    capasCards.clear();
}

function liberarGaleria() {
    galeria.forEach(item => URL.revokeObjectURL(item.url));
    galeria = [];
}

function formatarPreco(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        style: "currency", currency: "BRL"
    });
}

function mostrarVeiculos() {
    liberarCapas();
    lista.replaceChildren();
    document.getElementById("total-carros").textContent =
        `${carrosAdmin.length} ${carrosAdmin.length === 1 ? "veículo cadastrado" : "veículos cadastrados"}`;

    if (!carrosAdmin.length) {
        lista.textContent = "Nenhum veículo cadastrado.";
        return;
    }

    carrosAdmin.forEach(carro => {
        const card = document.createElement("article");
        card.className = "admin-veiculo";
        card.tabIndex = 0;
        card.setAttribute("aria-label", `Editar ${carro.marca} ${carro.modelo}`);
        card.addEventListener("click", evento => {
            if (!evento.target.closest("button")) abrirEdicao(carro);
        });
        card.addEventListener("keydown", evento => {
            if (evento.target === card && ["Enter", " "].includes(evento.key)) {
                evento.preventDefault();
                abrirEdicao(carro);
            }
        });
        const areaImagem = document.createElement("div");
        areaImagem.className = "admin-veiculo-imagem";
        areaImagem.textContent = "Imagem indisponível";

        if (carro.quantidadeFotos > 0) {
            carregarArquivoFoto(carro.id, 0).then(arquivo => {
                if (!card.isConnected) return;
                const url = URL.createObjectURL(arquivo);
                capasCards.add(url);
                const imagem = document.createElement("img");
                imagem.src = url;
                imagem.alt = `${carro.marca} ${carro.modelo}`;
                areaImagem.replaceChildren(imagem);
            }).catch(() => {});
        }

        const corpo = document.createElement("div");
        corpo.className = "admin-veiculo-corpo";
        const titulo = document.createElement("h3");
        titulo.textContent = `${carro.marca} ${carro.modelo}`;
        const info = document.createElement("p");
        info.textContent = `${carro.anoModelo} · ${carro.situacao} · ` +
            `${carro.quantidadeFotos || 0} ${carro.quantidadeFotos === 1 ? "foto" : "fotos"}`;
        const preco = document.createElement("strong");
        preco.textContent = formatarPreco(carro.preco);

        const acoes = document.createElement("div");
        acoes.className = "admin-veiculo-acoes";
        const editar = document.createElement("button");
        editar.type = "button";
        editar.className = "botao-secundario";
        editar.textContent = "Ver / Editar";
        editar.addEventListener("click", () => abrirEdicao(carro));
        const desativar = document.createElement("button");
        desativar.type = "button";
        desativar.className = "botao-excluir";
        desativar.textContent = "Desativar";
        desativar.disabled = carro.situacao === "INATIVO";
        desativar.addEventListener("click", () => desativarCarro(carro));
        acoes.append(editar, desativar);
        corpo.append(titulo, info, preco, acoes);
        card.append(areaImagem, corpo);
        lista.append(card);
    });
}

async function carregarVeiculosAdmin() {
    lista.textContent = "Carregando veículos...";
    try {
        carrosAdmin = await requisitar("/adm/carros");
        carrosAdmin.sort((a, b) => b.id - a.id);
        mostrarVeiculos();
    } catch (erro) {
        lista.textContent = "Não foi possível carregar os veículos.";
        mostrarMensagem(erro.message);
    }
}

function desenharGaleria() {
    miniaturas.replaceChildren();
    document.getElementById("contador-fotos").textContent = `${galeria.length} de 5 fotos`;

    if (!galeria.length) {
        miniaturas.textContent = "Nenhuma fotografia selecionada.";
        return;
    }

    galeria.forEach((foto, indice) => {
        const quadro = document.createElement("div");
        quadro.className = `admin-miniatura${indice === 0 ? " capa" : ""}`;
        const imagem = document.createElement("img");
        imagem.src = foto.url;
        imagem.alt = `Fotografia ${indice + 1} do veículo`;
        const botoes = document.createElement("div");
        botoes.className = "admin-miniatura-botoes";

        if (indice === 0) {
            const capa = document.createElement("span");
            capa.className = "capa-label";
            capa.textContent = "CAPA";
            botoes.append(capa);
        } else {
            const capa = document.createElement("button");
            capa.type = "button";
            capa.textContent = "Definir capa";
            capa.addEventListener("click", () => {
                galeria.unshift(galeria.splice(indice, 1)[0]);
                galeriaAlterada = true;
                desenharGaleria();
            });
            botoes.append(capa);
        }

        const remover = document.createElement("button");
        remover.type = "button";
        remover.textContent = "Remover";
        remover.setAttribute("aria-label", `Remover fotografia ${indice + 1}`);
        remover.addEventListener("click", () => {
            URL.revokeObjectURL(galeria[indice].url);
            galeria.splice(indice, 1);
            galeriaAlterada = true;
            desenharGaleria();
        });
        botoes.append(remover);
        quadro.append(imagem, botoes);
        miniaturas.append(quadro);
    });
}

function abrirModal() {
    formulario.reset();
    mostrarMensagem("", "erro", mensagemForm);
    liberarGaleria();
    galeriaAlterada = false;
    idEmEdicao = null;
    salvar.disabled = false;
    modal.showModal();
    desenharGaleria();
}

function abrirNovo() {
    versaoModal++;
    abrirModal();
    document.getElementById("titulo-form").textContent = "Cadastrar veículo";
    salvar.textContent = "Cadastrar veículo";
}

async function abrirEdicao(carro) {
    const versao = ++versaoModal;
    abrirModal();
    idEmEdicao = carro.id;
    document.getElementById("titulo-form").textContent = `Editar ${carro.marca} ${carro.modelo}`;
    salvar.textContent = "Salvar alterações";
    const campos = formulario.elements;
    for (const nome of ["marca", "modelo", "anoFabricacao", "anoModelo", "cor",
        "quilometragem", "combustivel", "transmissao", "preco", "descricao", "situacao"]) {
        campos.namedItem(nome).value = carro[nome];
    }

    // Fazemos o download das fotos existentes para mostrá-las e, se necessário,
    // reenviar somente a galeria resultante ao endpoint já existente.
    salvar.disabled = true;
    miniaturas.textContent = "Carregando fotografias...";
    const carregadas = [];
    try {
        for (let i = 0; i < (carro.quantidadeFotos || 0); i++) {
            const arquivo = await carregarArquivoFoto(carro.id, i);
            carregadas.push({ arquivo, url: URL.createObjectURL(arquivo) });
        }
        if (versao !== versaoModal || !modal.open) {
            carregadas.forEach(foto => URL.revokeObjectURL(foto.url));
            return;
        }
        galeria = carregadas;
        desenharGaleria();
        salvar.disabled = false;
    } catch (erro) {
        carregadas.forEach(foto => URL.revokeObjectURL(foto.url));
        if (versao !== versaoModal) return;
        mostrarMensagem("Não foi possível abrir a galeria. Tente novamente.", "erro", mensagemForm);
        miniaturas.textContent = "Erro ao carregar fotografias.";
    }
}

function obterDadosFormulario() {
    const campos = formulario.elements;
    const valor = nome => campos.namedItem(nome).value;
    return {
        marca: valor("marca").trim(),
        modelo: valor("modelo").trim(),
        anoFabricacao: Number(valor("anoFabricacao")),
        anoModelo: Number(valor("anoModelo")),
        cor: valor("cor").trim(),
        quilometragem: Number(valor("quilometragem")),
        combustivel: valor("combustivel").trim(),
        transmissao: valor("transmissao").trim(),
        preco: valor("preco"),
        descricao: valor("descricao").trim(),
        situacao: valor("situacao")
    };
}

function montarEnvioFotos() {
    const dados = new FormData();
    galeria.forEach(item => dados.append("fotos", item.arquivo));
    return dados;
}

formulario.elements.namedItem("fotos").addEventListener("change", evento => {
    const arquivos = Array.from(evento.target.files);
    evento.target.value = "";
    if (galeria.length + arquivos.length > 5) {
        mostrarMensagem("O limite é de 5 fotografias.", "erro", mensagemForm);
        return;
    }
    if (arquivos.some(foto => foto.size > 5 * 1024 * 1024 ||
        !["image/jpeg", "image/png", "image/webp"].includes(foto.type))) {
        mostrarMensagem("Use fotos JPG, PNG ou WebP de até 5 MB.", "erro", mensagemForm);
        return;
    }
    arquivos.forEach(arquivo => galeria.push({ arquivo, url: URL.createObjectURL(arquivo) }));
    if (arquivos.length) galeriaAlterada = true;
    mostrarMensagem("", "erro", mensagemForm);
    desenharGaleria();
});

formulario.addEventListener("submit", async evento => {
    evento.preventDefault();
    if (!formulario.reportValidity()) return;
    const editando = idEmEdicao !== null;
    if ((!editando || galeriaAlterada) && (galeria.length < 3 || galeria.length > 5)) {
        mostrarMensagem("Para salvar a galeria, deixe de 3 a 5 fotografias.", "erro", mensagemForm);
        return;
    }

    salvar.disabled = true;
    mostrarMensagem("", "erro", mensagemForm);
    let dadosSalvos = false;
    try {
        const dados = obterDadosFormulario();
        if (!editando) {
            const envio = montarEnvioFotos();
            envio.append("dados", new Blob([JSON.stringify(dados)], { type: "application/json" }));
            await requisitar("/carros", { method: "POST", body: envio });
        } else {
            await requisitar(`/carros/${idEmEdicao}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dados)
            });
            dadosSalvos = true;
            if (galeriaAlterada) {
                await requisitar(`/carros/${idEmEdicao}/fotos`, {
                    method: "PUT", body: montarEnvioFotos()
                });
            }
        }
        modal.close();
        await carregarVeiculosAdmin();
        mostrarMensagem(editando ? "Veículo atualizado com sucesso!" :
            "Veículo cadastrado com sucesso!", "sucesso");
    } catch (erro) {
        mostrarMensagem(dadosSalvos ? `Dados salvos, mas houve um erro nas fotos. ${erro.message}` :
            erro.message, "erro", mensagemForm);
        console.error(erro);
    } finally {
        salvar.disabled = false;
    }
});

async function desativarCarro(carro) {
    if (!confirm(`Deseja desativar ${carro.marca} ${carro.modelo}?`)) return;
    try {
        await requisitar(`/carros/${carro.id}`, { method: "DELETE" });
        await carregarVeiculosAdmin();
        mostrarMensagem("Veículo desativado com sucesso!", "sucesso");
    } catch (erro) {
        mostrarMensagem(erro.message);
    }
}

modal.addEventListener("close", () => {
    versaoModal++;
    liberarGaleria();
});
document.getElementById("novo-carro").addEventListener("click", abrirNovo);
document.getElementById("fechar-modal").addEventListener("click", () => modal.close());
document.getElementById("cancelar-edicao").addEventListener("click", () => modal.close());
document.getElementById("atualizar-lista").addEventListener("click", carregarVeiculosAdmin);
document.getElementById("botao-sair").addEventListener("click", () => {
    sessionStorage.removeItem("garagem_token");
    window.location.href = "/login.html";
});

if (!possuiPerfilAdmin(tokenAdmin)) {
    aviso.hidden = false;
    aviso.textContent = "Acesso restrito. Entre com uma conta de administrador.";
} else {
    aviso.hidden = true;
    painel.hidden = false;
    carregarVeiculosAdmin();
}
