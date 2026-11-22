async function carregarDetalhes() {
    const parametros = new URLSearchParams(window.location.search);
    const id = parametros.get("id");

    const mensagem = document.getElementById("mensagem-detalhes");
    const conteudo = document.getElementById("conteudo-detalhes");

    if (!id || !/^\d+$/.test(id)) {
        mensagem.textContent = "Veículo inválido.";
        return;
    }

    try {
        const resposta = await fetch(`/carros/${id}`);

        if (!resposta.ok) {
            throw new Error("Veículo não encontrado.");
        }

        const carro = await resposta.json();

        document.getElementById("carro-nome").textContent =
            `${carro.marca} ${carro.modelo}`;

        document.title = `${carro.marca} ${carro.modelo} | GARAGEM`;

        document.getElementById("carro-preco").textContent =
            Number(carro.preco).toLocaleString("pt-BR", {
                style: "currency",
                currency: "BRL"
            });

        function preencher(campo, valor) {
            document.getElementById(campo).textContent =
                valor ?? "Não informado";
        }

        preencher("carro-marca", carro.marca);
        preencher("carro-modelo", carro.modelo);
        preencher("carro-ano-fabricacao", carro.anoFabricacao);
        preencher("carro-ano-modelo", carro.anoModelo);
        preencher("carro-cor", carro.cor);
        preencher("carro-combustivel", carro.combustivel);
        preencher("carro-transmissao", carro.transmissao);

        const quilometragem = Number(
            carro.quilometragem
        ).toLocaleString("pt-BR");

        preencher("carro-quilometragem", `${quilometragem} km`);

        preencher(
            "carro-descricao",
            carro.descricao || "Descrição não disponível."
        );

        const areaImagem = document.getElementById("imagem-detalhes");

        function mostrarSemFoto() {
            areaImagem.textContent = "Imagem indisponível";
            areaImagem.classList.add("imagem-indisponivel");
        }

        if (carro.fotoPrincipal) {
            const imagem = document.createElement("img");

            imagem.src = `/carros/${carro.id}/foto`;
            imagem.alt = `${carro.marca} ${carro.modelo}`;
            imagem.onerror = mostrarSemFoto;

            areaImagem.appendChild(imagem);
        } else {
            mostrarSemFoto();
        }

        mensagem.hidden = true;
        conteudo.hidden = false;

    } catch (erro) {
        mensagem.textContent =
            "Não foi possível carregar os detalhes deste veículo.";

        console.error(erro);
    }
}

carregarDetalhes();