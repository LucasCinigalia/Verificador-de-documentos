package br.com.veridoc.api.decisao.regras;

/** Validação de CPF pelos dígitos verificadores (módulo 11). */
public final class Cpf {

    private Cpf() {
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean valido(String valor) {
        String cpf = somenteDigitos(valor);
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digito(cpf, 9) == cpf.charAt(9) - '0'
                && digito(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digito(String cpf, int posicao) {
        int soma = 0;
        for (int i = 0; i < posicao; i++) {
            soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
