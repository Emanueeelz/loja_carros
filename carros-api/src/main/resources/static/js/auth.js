const CHAVE_TOKEN = "garagem_token";

// Exibe mensagens de login e cadastro
function mostrarMensagem(texto, tipo = "erro") {
    const mensagem = document.getElementById("mensagem-auth");

    if (!mensagem) return;

    mensagem.textContent = texto;
    mensagem.className = `mensagem-auth ${tipo}`;
}

// Lê os dados do JWT
function obterDadosToken(token) {
    if (!token) return null;

    try {
        const parte = token.split(".")[1]
            .replace(/-/g, "+")
            .replace(/_/g, "/");

        const dados = JSON.parse(atob(parte));

        // Verifica se o token expirou
        if (!dados.exp || dados.exp <= Date.now() / 1000) {
            return null;
        }

        return dados;

    } catch (erro) {
        return null;
    }
}

// Atualiza o menu conforme a sessão
function atualizarMenu() {
    const token = sessionStorage.getItem(CHAVE_TOKEN);
    const dados = obterDadosToken(token);

    // Remove tokens inválidos ou expirados
    if (token && !dados) {
        sessionStorage.removeItem(CHAVE_TOKEN);
    }

    const entrar = document.getElementById("link-entrar");
    const cadastro = document.getElementById("link-cadastro");
    const painel = document.getElementById("link-admin");
    const sair = document.getElementById("botao-sair");

    const conectado = Boolean(dados);
    const administrador = dados?.perfil === "ADMIN";

    if (entrar) entrar.hidden = conectado;
    if (cadastro) cadastro.hidden = conectado;
    if (painel) painel.hidden = !administrador;
    if (sair) sair.hidden = !conectado;

    // Encerrar sessão
    if (sair) {
        sair.addEventListener("click", () => {
            sessionStorage.removeItem(CHAVE_TOKEN);
            window.location.href = "/";
        });
    }
}

atualizarMenu();

// Login
const formLogin = document.getElementById("form-login");

if (formLogin) {
    const parametros = new URLSearchParams(window.location.search);

    if (parametros.get("cadastrado") === "1") {
        mostrarMensagem(
            "Conta criada com sucesso! Agora faça login.",
            "sucesso"
        );
    }

    formLogin.addEventListener("submit", async evento => {
        evento.preventDefault();

        const email = document.getElementById("email").value.trim();
        const senha = document.getElementById("senha").value;
        const botao = formLogin.querySelector("button[type='submit']");

        botao.disabled = true;
        mostrarMensagem("");

        try {
            const resposta = await fetch("/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, senha })
            });

            if (resposta.status === 401) {
                mostrarMensagem("E-mail ou senha inválidos.");
                return;
            }

            if (!resposta.ok) {
                throw new Error("Erro ao fazer login.");
            }

            const dados = await resposta.json();

            if (!dados.token) {
                throw new Error("Token não recebido.");
            }

            // Salva o JWT nesta sessão do navegador
            sessionStorage.setItem(CHAVE_TOKEN, dados.token);

            // Retorna à página inicial
            window.location.href = "/";

        } catch (erro) {
            mostrarMensagem(
                "Não foi possível entrar. Tente novamente."
            );

            console.error(erro);

        } finally {
            botao.disabled = false;
        }
    });
}

// Cadastro
const formCadastro = document.getElementById("form-cadastro");

if (formCadastro) {
    formCadastro.addEventListener("submit", async evento => {
        evento.preventDefault();

        const nome = document.getElementById("nome").value.trim();
        const email = document.getElementById("email").value.trim();
        const senha = document.getElementById("senha").value;
        const confirmar = document.getElementById("confirmar-senha").value;

        const botao = formCadastro.querySelector("button[type='submit']");

        mostrarMensagem("");

        if (!nome) {
            mostrarMensagem("Informe seu nome.");
            return;
        }

        if (senha !== confirmar) {
            mostrarMensagem("As senhas não coincidem.");
            return;
        }

        botao.disabled = true;

        try {
            const resposta = await fetch("/usuarios", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ nome, email, senha })
            });

            if (resposta.status === 409) {
                mostrarMensagem("Este e-mail já está cadastrado.");
                return;
            }

            if (resposta.status === 400) {
                mostrarMensagem("Confira os dados informados.");
                return;
            }

            if (!resposta.ok) {
                throw new Error("Erro ao cadastrar usuário.");
            }

            // Cadastro concluído: encaminha para o login
            window.location.href = "/login.html?cadastrado=1";

        } catch (erro) {
            mostrarMensagem(
                "Não foi possível criar sua conta. Tente novamente."
            );

            console.error(erro);

        } finally {
            botao.disabled = false;
        }
    });
}
