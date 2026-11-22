async function carregarCarros() {
    const lista = document.getElementById("lista-carros");

    const marca = document.getElementById("marca").value.trim();
    const modelo = document.getElementById("modelo").value.trim();
    const precoMax = document.getElementById("precoMax").value;

    const parametros = new URLSearchParams();

    if (marca) parametros.set("marca", marca);
    if (modelo) parametros.set("modelo", modelo);
    if (precoMax) parametros.set("precoMax", precoMax);

    lista.textContent = "Carregando veículos...";

    try {
        const url = parametros.toString()
            ? `/carros?${parametros.toString()}`
            : "/carros";

        const resposta = await fetch(url);

        if (!resposta.ok) {
            throw new Error("Erro ao carregar os veículos.");
        }

        const carros = await resposta.json();
        lista.innerHTML = "";

        if (carros.length === 0) {
            const mensagem = document.createElement("p");
            mensagem.className = "mensagem-catalogo";
            mensagem.textContent = parametros.toString()
                ? "Nenhum veículo encontrado para esses filtros."
                : "Nenhum veículo disponível no momento.";

            lista.appendChild(mensagem);
            return;
        }

        carros.forEach(carro => {
            const card = document.createElement("a");
            card.className = "card-carro";
            card.href = `/detalhes.html?id=${carro.id}`;

            if (carro.fotoPrincipal) {
                const imagem = document.createElement("img");
                imagem.src = `/carros/${carro.id}/foto`;
                imagem.alt = `${carro.marca} ${carro.modelo}`;
                imagem.loading = "lazy";

                imagem.onerror = () => {
                    const semFoto = document.createElement("div");
                    semFoto.className = "sem-foto";
                    semFoto.textContent = "Imagem indisponível";
                    imagem.replaceWith(semFoto);
                };

                card.appendChild(imagem);
            } else {
                const semFoto = document.createElement("div");
                semFoto.className = "sem-foto";
                semFoto.textContent = "Imagem indisponível";
                card.appendChild(semFoto);
            }

            const info = document.createElement("div");
            info.className = "card-info";

            const titulo = document.createElement("h3");
            titulo.textContent = `${carro.marca} ${carro.modelo}`;

            const detalhes = document.createElement("p");
            const quilometragem = Number(
                carro.quilometragem
            ).toLocaleString("pt-BR");

            detalhes.textContent =
                `${carro.anoModelo} • ${quilometragem} km`;

            const preco = document.createElement("strong");
            preco.textContent = Number(carro.preco).toLocaleString(
                "pt-BR",
                {
                    style: "currency",
                    currency: "BRL"
                }
            );

            info.append(titulo, detalhes, preco);
            card.appendChild(info);
            lista.appendChild(card);
        });

    } catch (erro) {
        lista.textContent = "Não foi possível carregar os veículos.";
        console.error(erro);
    }
}

document.getElementById("filtros").addEventListener(
    "submit",
    evento => {
        evento.preventDefault();
        carregarCarros();
    }
);

document.getElementById("limpar-filtros").addEventListener(
    "click",
    () => {
        document.getElementById("filtros").reset();
        carregarCarros();
    }
);

carregarCarros();