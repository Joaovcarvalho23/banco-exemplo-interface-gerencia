package br.ufrpe.poo.banco.negocio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import br.ufrpe.poo.banco.exceptions.ClienteJaPossuiContaException;
import br.ufrpe.poo.banco.exceptions.ClienteNaoPossuiContaException;

/**
 * Classe de teste respons�vel por testar as condi��es dos m�todos
 * adicionarConta e removerConta da classe Cliente.
 * 
 * @author Aluno
 * 
 */
public class TesteCliente {

	/**
	 * Testa a inser��o de uma nova conta vinculada ao cliente
	 */
	@Test
	public void adicionarContaTest() {
		Cliente c1 = new Cliente("nome", "123");
		try {
			c1.adicionarConta("1");
		} catch (ClienteJaPossuiContaException e) {
			fail();
		}
		assertEquals(c1.procurarConta("1"), 0);
	}

	/**
	 * Testa a condi��o da tentativa de adicionar uma conta j� existente � lista
	 * de contas do cliente
	 * 
	 * @throws ClienteJaPossuiContaException
	 */
	@Test(expected = ClienteJaPossuiContaException.class)
	public void adicionarContaJaExistenteTest()
			throws ClienteJaPossuiContaException {
		Cliente c1 = new Cliente("nome", "123");
		c1.adicionarConta("1"); // adiciona a conta a 1� vez
		c1.adicionarConta("1"); // tentativa de adicionar a mesma conta
								// novamente
	}

	/**
	 * Teste a remo��o de uma conta da lista de contas do cliente
	 */
	@Test
	public void removerContaClienteTest() {
		Cliente c1 = new Cliente("nome", "123");
		try {
			c1.adicionarConta("1"); // adiciona conta com n�mero 1
			c1.removerConta("1"); // remove a conta de n�mero 1
		} catch (Exception e) {
			fail("Exce��o inesperada lancada!");
		}

		assertEquals(c1.procurarConta("1"), -1);
	}

	/**
	 * Testa a remo��o de uma determinada conta que n�o est� vinculada ao
	 * cliente
	 * 
	 * @throws ClienteNaoPossuiContaException
	 */
	@Test(expected = ClienteNaoPossuiContaException.class)
	public void removerContaClienteSemContaTest()
			throws ClienteNaoPossuiContaException {
		Cliente c1 = new Cliente("nome", "123");
		c1.removerConta("1"); // tenta remover a conta de um cliente sem contas
	}

	/**
	 * Testa a consulta do numero da conta
	 *
	 */
	@Test
	public void consultarNumeroContaTest() {
		Cliente c1 = new Cliente("nome", "123");
		String numeroConta = "1";
		int indexConta = 0;
		try {
			c1.adicionarConta(numeroConta);
			indexConta = c1.procurarConta(numeroConta);
		} catch (ClienteJaPossuiContaException e) {
			fail();
		}
		assertEquals(c1.consultarNumeroConta(indexConta), numeroConta);
	}
	/**
	 * Testa a remoção de todas as contas
	 *
	 */
	@Test
	public void removerTodasContaClienteTest() {
		Cliente c1 = new Cliente("nome", "123");
		try {
			c1.adicionarConta("1"); // adiciona conta com n�mero 1
			c1.adicionarConta("2"); // adiciona conta com n�mero 2
			c1.removerTodasAsContas(); // remove todas as conta
		} catch (Exception e) {
			fail("Exce��o inesperada lancada!");
		}

		assertNull(c1.getContas());
	}

	/**
	 * Testa a consulta do cpf
	 *
	 */
	@Test
	public void consultarCpfClienteTest() {
		String cpf = "123";
		Cliente c1 = new Cliente("nome", cpf);

		assertEquals(c1.getCpf(), cpf);
	}

	/**
	 * Testa a alteração do cpf
	 *
	 */
	@Test
	public void alterarCpfClienteTest() {
		String cpfAntigo = "123";
		String cpfNovo = "456";

		Cliente c1 = new Cliente("nome", cpfAntigo);
		c1.setCpf(cpfNovo);


		assertEquals(c1.getCpf(), cpfNovo);
	}

	/**
	 * Testa a consulta do nome
	 *
	 */
	@Test
	public void consultarNomeClienteTest() {
		String nome = "nome";
		Cliente c1 = new Cliente(nome, "123");

		assertEquals(c1.getNome(), nome);
	}

	/**
	 * Testa a alteração do cpf
	 *
	 */
	@Test
	public void alterarNomeClienteTest() {
		String nomeAntigo = "João";
		String nomeNovo = "Maria";

		Cliente c1 = new Cliente(nomeAntigo, "123");
		c1.setNome(nomeNovo);


		assertEquals(c1.getNome(), nomeNovo);
	}

	/**
	 * Testa o método equals
	 */
	@Test
	public void equalsClienteTest() {
		Cliente c1 = new Cliente("nome", "123");
		Cliente c2 = new Cliente("nome", "123");
		Cliente c3 = new Cliente("outroNome", "456");

		// Testa igualdade de objetos com mesmo CPF
		assertEquals(c1, c2);

		// Testa desigualdade de objetos com CPF diferentes
		assertNotEquals(c1, c3);

		// Testa igualdade reflexiva
		assertEquals(c1, c1);

		// Testa igualdade simétrica
		assertEquals(c1.equals(c2), c2.equals(c1));

		// Testa igualdade transitiva
		Cliente c4 = new Cliente("nome", "123");
		assertTrue(c1.equals(c2) && c2.equals(c4) && c1.equals(c4));

		// Testa comparacao com objeto null
		assertNotEquals(c1, null);

		// Testa comparacao com objeto de outro tipo
		assertNotEquals(c1, new Object());
	}

	/**
	 * Testa o método toString
	 */
	@Test
	public void toStringClienteTest() {
		Cliente c1 = new Cliente("nome", "123");

		// Adiciona contas para teste
		try {
			c1.adicionarConta("1");
			c1.adicionarConta("2");
		} catch (ClienteJaPossuiContaException e) {
			fail();
		}

		String expected = "Nome: nome\nCPF: 123\nContas: [1, 2]";
		assertEquals(expected, c1.toString());
	}
}
