function somenteDigitos(valor, limite) {
    return valor.replace(/\D/g, "").slice(0, limite);
}

function formatarCpf(valor) {
    const digitos = somenteDigitos(valor, 11);

    return digitos
        .replace(/^(\d{3})(\d)/, "$1.$2")
        .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
        .replace(/\.(\d{3})(\d)/, ".$1-$2");
}

function formatarTelefone(valor) {
    const digitos = somenteDigitos(valor, 11);

    if (digitos.length === 0) {
        return "";
    }

    if (digitos.length <= 2) {
        return `(${digitos}`;
    }

    const ddd = digitos.slice(0, 2);
    const numero = digitos.slice(2);
    const quantidadeAntesDoHifen = digitos.length === 11 ? 5 : 4;
    const primeiraParte = numero.slice(0, quantidadeAntesDoHifen);
    const segundaParte = numero.slice(quantidadeAntesDoHifen);

    return segundaParte
        ? `(${ddd}) ${primeiraParte}-${segundaParte}`
        : `(${ddd}) ${primeiraParte}`;
}

function formatarMoeda(valor) {
    const digitos = somenteDigitos(valor, 15);

    if (digitos.length === 0) {
        return "";
    }

    return (Number(digitos) / 100).toLocaleString("pt-BR", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function obterValorMonetario(campo) {
    if (!campo.value) {
        return Number.NaN;
    }

    const valorNormalizado = campo.value
        .replace(/\./g, "")
        .replace(",", ".");

    return Number(valorNormalizado);
}

const formatadores = {
    cpf: formatarCpf,
    telefone: formatarTelefone,
    moeda: formatarMoeda
};

document.querySelectorAll("[data-mascara]").forEach((campo) => {
    const formatador = formatadores[campo.dataset.mascara];

    if (!formatador) {
        return;
    }

    campo.addEventListener("input", () => {
        campo.value = formatador(campo.value);
    });
});

document.querySelectorAll("[data-somente-digitos]").forEach((campo) => {
    campo.addEventListener("input", () => {
        const limite = Number(campo.maxLength) > 0
            ? Number(campo.maxLength)
            : campo.value.length;

        campo.value = somenteDigitos(campo.value, limite);
    });
});
