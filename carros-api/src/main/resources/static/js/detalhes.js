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

        function preencher(campo, valor) {
            document.getElementById(campo).textContent =
                valor ?? "Não informado";
        }

        preencher("carro-nome", `${carro.marca} ${carro.modelo}`);
        document.title = `${carro.marca} ${carro.modelo} | GARAGEM`;

        preencher(
            "carro-preco",
            Number(carro.preco).toLocaleString("pt-BR", {
                style: "currency", currency: "BRL"
            })
        );

        preencher("carro-ano-fabricacao", carro.anoFabricacao);
        preencher("carro-ano-modelo", carro.anoModelo);
        preencher("carro-cor", carro.cor);
        preencher("carro-combustivel", carro.combustivel);
        preencher("carro-transmissao", carro.transmissao);

        const quilometragem = Number(carro.quilometragem)
            .toLocaleString("pt-BR");
        preencher("carro-quilometragem", `${quilometragem} km`);

        preencher(
            "carro-descricao",
            carro.descricao || "Descrição não disponível."
        );

        // A primeira fotografia é sempre a capa.
        const areaImagem = document.getElementById("imagem-detalhes");
        const miniaturas = document.getElementById("galeria-miniaturas");
        const quantidade = Number(carro.quantidadeFotos) ||
            (carro.fotoPrincipal ? 1 : 0);

        areaImagem.replaceChildren();
        areaImagem.classList.remove("imagem-indisponivel");
        miniaturas.replaceChildren();
        miniaturas.hidden = true;

        function mostrarSemFoto() {
            areaImagem.textContent = "Imagem indisponível";
            areaImagem.classList.add("imagem-indisponivel");
        }

        function mostrarFoto(indice) {
            const imagem = document.createElement("img");
            imagem.src = `/carros/${carro.id}/fotos/${indice}`;
            imagem.alt = `${carro.marca} ${carro.modelo} — fotografia ${indice + 1}`;
            imagem.onerror = mostrarSemFoto;

            areaImagem.classList.remove("imagem-indisponivel");
            areaImagem.replaceChildren(imagem);

            miniaturas.querySelectorAll("button").forEach((botao, posicao) => {
                const ativa = posicao === indice;
                botao.classList.toggle("ativa", ativa);
                botao.setAttribute("aria-pressed", String(ativa));
            });
        }

        if (quantidade > 0) {
            if (quantidade > 1) {
                for (let indice = 0; indice < Math.min(quantidade, 5); indice++) {
                    const botao = document.createElement("button");
                    botao.type = "button";
                    botao.className = "miniatura-botao";
                    botao.setAttribute("aria-label", `Ver fotografia ${indice + 1}`);
                    botao.setAttribute("aria-pressed", "false");

                    const miniatura = document.createElement("img");
                    miniatura.src = `/carros/${carro.id}/fotos/${indice}`;
                    miniatura.alt = `Fotografia ${indice + 1} do veículo`;
                    miniatura.loading = "lazy";

                    botao.appendChild(miniatura);
                    botao.addEventListener("click", () => mostrarFoto(indice));
                    miniaturas.appendChild(botao);
                }
                miniaturas.hidden = false;
            }

            mostrarFoto(0);
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