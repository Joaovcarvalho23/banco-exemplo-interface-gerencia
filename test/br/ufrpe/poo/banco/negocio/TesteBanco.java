package br.ufrpe.poo.banco.negocio;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import br.ufrpe.poo.banco.dados.RepositorioClientesArquivoBin;
import br.ufrpe.poo.banco.dados.RepositorioClientesArray;
import br.ufrpe.poo.banco.exceptions.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import br.ufrpe.poo.banco.dados.RepositorioContasArquivoBin;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TesteBanco {

	private static Banco banco;
	private RepositorioContasArquivoBin repositorioContas;
	private Poupanca poupanca;
	private ContaAbstrata contaNaoPoupanca;

	@Before
	public void apagarArquivos() throws IOException, RepositorioException,
			InicializacaoSistemaException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("clientes.dat"));
		bw.close();
		bw = new BufferedWriter(new FileWriter("contas.dat"));
		bw.close();
		
		Banco.instance = null;
		TesteBanco.banco = Banco.getInstance();
	}

	/**
	 * Verifica o cadastramento de uma nova conta.
	 * 
	 */
	@Test
	public void testeCadastarNovaConta() throws RepositorioException,
			ContaJaCadastradaException, ContaNaoEncontradaException,
			InicializacaoSistemaException {

		Banco banco = new Banco(null, new RepositorioContasArquivoBin());
		ContaAbstrata conta1 = new Conta("1", 100);
		banco.cadastrar(conta1);
		ContaAbstrata conta2 = banco.procurarConta("1");
		assertEquals(conta1.getNumero(), conta2.getNumero());
		assertEquals(conta1.getSaldo(), conta2.getSaldo(), 0);
	}

	/**
	 * Verifica que nao e permitido cadastrar duas contas com o mesmo numero.
	 * 
	 */
	@Test(expected = ContaJaCadastradaException.class)
	public void testeCadastrarContaExistente() throws RepositorioException,
			ContaJaCadastradaException, ContaNaoEncontradaException,
			InicializacaoSistemaException {

		Conta c1 = new Conta("1", 200);
		Conta c2 = new Conta("1", 300);
		banco.cadastrar(c1);
		banco.cadastrar(c2);
		fail("Excecao ContaJaCadastradaException nao levantada");
	}

	/**
	 * Verifica se o credito esta sendo executado corretamente em uma conta
	 * corrente.
	 * 
	 */
	@Test
	public void testeCreditarContaExistente() throws RepositorioException,
			ContaNaoEncontradaException, InicializacaoSistemaException,
			ContaJaCadastradaException, ValorInvalidoException {

		ContaAbstrata conta = new Conta("1", 100);
		banco.cadastrar(conta);
		banco.creditar(conta, 100);
		conta = banco.procurarConta("1");
		assertEquals(200, conta.getSaldo(), 0);
	}

	@Test(expected = ValorInvalidoException.class)
	public void testeCreditarValorNegativo() throws RepositorioException,
			ContaNaoEncontradaException, InicializacaoSistemaException,
			ContaJaCadastradaException, ValorInvalidoException {

		ContaAbstrata conta = new Conta("1", 100);
		banco.cadastrar(conta);
		banco.creditar(conta, -100);
		fail("O valor informado eh invalido!");
	}

	/**
	 * Verifica a excecao levantada na tentativa de creditar em uma conta que
	 * nao existe.
	 * 
	 */
	@Test(expected = ContaNaoEncontradaException.class)
	public void testeCreditarContaInexistente() throws RepositorioException,
			ContaNaoEncontradaException, InicializacaoSistemaException,
			ValorInvalidoException {

		banco.creditar(new Conta("", 0), 200);

		fail("Excecao ContaNaoEncontradaException nao levantada");
	}

	/**
	 * Verifica que a operacao de debito em conta corrente esta acontecendo
	 * corretamente.
	 * 
	 */
	@Test
	public void testeDebitarContaExistente() throws RepositorioException,
			ContaNaoEncontradaException, SaldoInsuficienteException,
			InicializacaoSistemaException, ContaJaCadastradaException,
			ValorInvalidoException {

		ContaAbstrata conta = new Conta("1", 50);
		banco.cadastrar(conta);
		banco.debitar(conta, 50);
		conta = banco.procurarConta("1");
		assertEquals(0, conta.getSaldo(), 0);
	}

	/**
	 * Verifica que tentantiva de debitar em uma conta que nao existe levante
	 * excecao.
	 * 
	 */
	@Test(expected = ContaNaoEncontradaException.class)
	public void testeDebitarContaInexistente() throws RepositorioException,
			ContaNaoEncontradaException, SaldoInsuficienteException,
			InicializacaoSistemaException, ValorInvalidoException {

		banco.debitar(new Conta("", 0), 50);
		fail("Excecao ContaNaoEncontradaException nao levantada");
	}

	/**
	 * Verifica que a transferencia entre contas correntes e realizada com
	 * sucesso.
	 * 
	 */
	@Test
	public void testeTransferirContaExistente() throws RepositorioException,
			ContaNaoEncontradaException, SaldoInsuficienteException,
			InicializacaoSistemaException, ContaJaCadastradaException,
			ValorInvalidoException {

		ContaAbstrata conta1 = new Conta("1", 100);
		ContaAbstrata conta2 = new Conta("2", 200);
		banco.cadastrar(conta1);
		banco.cadastrar(conta2);
		banco.transferir(conta1, conta2, 50);
		conta1 = banco.procurarConta("1");
		conta2 = banco.procurarConta("2");
		assertEquals(50, conta1.getSaldo(), 0);
		assertEquals(250, conta2.getSaldo(), 0);
	}

	/**
	 * Verifica que tentativa de transferir entre contas cujos numeros nao
	 * existe levanta excecao.
	 * 
	 */
	@Test(expected = ContaNaoEncontradaException.class)
	public void testeTransferirContaInexistente() throws RepositorioException,
			ContaNaoEncontradaException, SaldoInsuficienteException,
			InicializacaoSistemaException, ValorInvalidoException {

		banco.transferir(new Conta("", 0), new Conta("", 0), 50);
		fail("Excecao ContaNaoEncontradaException nao levantada)");
	}

	/**
	 * Verifica que render juros de uma conta poupanca funciona corretamente
	 * 
	 */
	@Ignore
	@Test
	public void testeRenderJurosContaExistente() throws RepositorioException,
			ContaNaoEncontradaException, RenderJurosPoupancaException,
			InicializacaoSistemaException, ContaJaCadastradaException {

		Poupanca poupanca = new Poupanca("20", 100);
		banco.cadastrar(poupanca);
		double saldoSemJuros = poupanca.getSaldo();
		double saldoComJuros = saldoSemJuros + (saldoSemJuros * 0.008);
		poupanca.renderJuros(0.008);
		assertEquals(saldoComJuros, poupanca.getSaldo(), 0);
	}

	/**
	 * Verifica que tentativa de render juros em conta inexistente levanta
	 * excecao.
	 * 
	 */

	@Test
	public void testeRenderJurosContaPoupancaExistente() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException, InicializacaoSistemaException, ContaJaCadastradaException {
		Poupanca poupanca = new Poupanca("20", 100);
		banco.cadastrar(poupanca);

		double saldoSemJuros = poupanca.getSaldo();
		double saldoEsperado = saldoSemJuros + (saldoSemJuros * 0.5 / 100);

		banco.renderJuros(poupanca);
		poupanca = (Poupanca) banco.procurarConta("20");
		assertEquals(saldoEsperado, poupanca.getSaldo(), 0.01);
	}

	@Test(expected = RenderJurosPoupancaException.class)
	public void testeRenderJurosContaNaoEhPoupanca() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException, InicializacaoSistemaException, ContaJaCadastradaException {
		ContaAbstrata contaCorrente = new Conta("30", 100);
		banco.cadastrar(contaCorrente);
		banco.renderJuros(contaCorrente);
	}

	@Test(expected = ContaNaoEncontradaException.class)
	public void testeRenderJurosContaInexistente() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException, InicializacaoSistemaException {
		Poupanca poupancaInexistente = new Poupanca("40", 100);

		banco.renderJuros(poupancaInexistente);
	}

	/**
	 * Verifica que render bonus de uma conta especial funciona corretamente.
	 * 
	 */
	@Test
	public void testeRenderBonusContaEspecialExistente() throws RepositorioException, ContaNaoEncontradaException, RenderBonusContaEspecialException, InicializacaoSistemaException, ContaJaCadastradaException {
		RepositorioContasArquivoBin mockRepositorioContas = mock(RepositorioContasArquivoBin.class);
		Banco banco = new Banco(new RepositorioClientesArquivoBin(), mockRepositorioContas);

		ContaAbstrata contaEspecial = new ContaEspecial("12345", 500);

		when(mockRepositorioContas.existe(contaEspecial.getNumero())).thenReturn(true);
		Mockito.doNothing().when(mockRepositorioContas).atualizar(contaEspecial);

		banco.renderBonus(contaEspecial);

		verify(mockRepositorioContas, Mockito.times(1)).existe(contaEspecial.getNumero());
		verify(mockRepositorioContas, Mockito.times(1)).atualizar(contaEspecial);
		verify((ContaEspecial) contaEspecial, Mockito.times(1)).renderBonus();
	}


	/**
	 * Verifica que a tentativa de render bonus em inexistente levanta excecao.
	 * 
	 */
	@Ignore
	@Test(expected = ContaNaoEncontradaException.class)
	public void testeRenderBonusContaEspecialNaoInexistente()
			throws RepositorioException, ContaNaoEncontradaException,
			RenderBonusContaEspecialException, InicializacaoSistemaException,
			RenderJurosPoupancaException {

		fail("Nao implementado");
	}

	/**
	 * Verifica que tentativa de render bonus em conta que nao e especial
	 * levante excecao.
	 */
	@Ignore
	@Test(expected = RenderBonusContaEspecialException.class)
	public void testeRenderBonusContaNaoEspecial() throws RepositorioException,
			ContaNaoEncontradaException, RenderBonusContaEspecialException,
			InicializacaoSistemaException, RenderJurosPoupancaException,
			ContaJaCadastradaException {

		fail("Nao implementado");
	}

	@Test
	public void testeCadastrarClienteNovo() throws RepositorioException, ClienteJaCadastradoException, ClienteJaCadastradoException {
		Cliente cliente = new Cliente("Joao", "123456789");
		banco.cadastrarCliente(cliente);
		Cliente clienteCadastrado = banco.procurarCliente("123456789");
		assertEquals(cliente.getNome(), clienteCadastrado.getNome());
		assertEquals(cliente.getCpf(), clienteCadastrado.getCpf());
	}

	@Test(expected = ClienteJaCadastradoException.class)
	public void testeCadastrarClienteExistente() throws RepositorioException, ClienteJaCadastradoException {
		Cliente cliente = new Cliente("Karina", "987654321");
		banco.cadastrarCliente(cliente);
		banco.cadastrarCliente(cliente);
		fail("Exceção ClienteJaCadastradoException não levantada");
	}

	@Test
	public void testeAssociarContaComSucesso() throws RepositorioException, ClienteJaPossuiContaException,
            ContaJaAssociadaException, ClienteNaoCadastradoException, ClienteJaCadastradoException, ContaJaCadastradaException {
		Cliente cliente = new Cliente("João", "123456789");
		ContaAbstrata conta = new Conta("1", 100);

		banco.cadastrarCliente(cliente);
		banco.cadastrar(conta);
		banco.associarConta(cliente.getCpf(), conta.getNumero());

		Cliente clienteAssociado = banco.procurarCliente(cliente.getCpf());
		assertTrue(clienteAssociado.getContas().contains(conta.getNumero()));
	}

	@Test(expected = ClienteNaoCadastradoException.class)
	public void testeAssociarContaClienteNaoCadastrado() throws RepositorioException, ClienteJaPossuiContaException,
			ContaJaAssociadaException, ClienteNaoCadastradoException {
		banco.associarConta("987654321", "1");

		fail("Exceção ClienteNaoCadastradoException não levantada");
	}

	@Test(expected = ContaJaAssociadaException.class)
	public void testeAssociarContaJaAssociada() throws RepositorioException, ClienteJaPossuiContaException,
            ContaJaAssociadaException, ClienteNaoCadastradoException, ClienteJaCadastradoException, ContaJaCadastradaException {
		Cliente cliente = new Cliente("Maria", "987654321");
		ContaAbstrata conta = new Conta("2", 200);

		banco.cadastrarCliente(cliente);
		banco.cadastrar(conta);
		banco.associarConta(cliente.getCpf(), conta.getNumero());
		banco.associarConta(cliente.getCpf(), conta.getNumero());

		fail("Exceção ContaJaAssociadaException não levantada");
	}

	@Test(expected = ClienteJaPossuiContaException.class)
	public void testeAssociarContaClienteJaPossuiConta() throws RepositorioException, ClienteJaPossuiContaException,
            ContaJaAssociadaException, ClienteNaoCadastradoException, ClienteJaCadastradoException, ContaJaCadastradaException {
		Cliente cliente = new Cliente("Carlos", "456789123");
		ContaAbstrata conta1 = new Conta("3", 300);
		ContaAbstrata conta2 = new Conta("4", 400);

		banco.cadastrarCliente(cliente);
		banco.cadastrar(conta1);
		banco.cadastrar(conta2);
		banco.associarConta(cliente.getCpf(), conta1.getNumero());

		banco.associarConta(cliente.getCpf(), conta2.getNumero());

		fail("Exceção ClienteJaPossuiContaException não levantada");
	}

	@Test
	public void testeAssociarContaContaNula() throws RepositorioException, ClienteJaPossuiContaException,
			ContaJaAssociadaException, ClienteNaoCadastradoException {
		RepositorioClientesArquivoBin mockRepositorioClientes = mock(RepositorioClientesArquivoBin.class);
		RepositorioContasArquivoBin mockRepositorioContas = mock(RepositorioContasArquivoBin.class);
		Banco banco = new Banco(mockRepositorioClientes, mockRepositorioContas);

		Cliente cliente = new Cliente("João", "123456789");
		when(mockRepositorioClientes.procurar(cliente.getCpf())).thenReturn(cliente);
		when(mockRepositorioContas.procurar("001")).thenReturn(null);
		when(mockRepositorioClientes.atualizar(cliente)).thenReturn(true);

		try {
			banco.associarConta(cliente.getCpf(), "001");
		} catch (Exception e) {
			fail("Não era esperado uma exceção. Exceção: " + e.getMessage());
		}
		verify(mockRepositorioClientes, Mockito.times(1)).atualizar(cliente);
		assertTrue(cliente.getContas().contains("001"));
	}

	@Test
	public void testeRemoverClienteComContas() throws RepositorioException, ClienteNaoCadastradoException,
			ContaNaoEncontradaException, ClienteNaoPossuiContaException, ContaJaCadastradaException, ClienteJaCadastradoException, ClienteJaPossuiContaException, ContaJaAssociadaException {
		Cliente cliente = new Cliente("Bruno", "987654321");
		ContaAbstrata conta1 = new Conta("1", 100);
		ContaAbstrata conta2 = new Conta("2", 200);
		banco.cadastrarCliente(cliente);
		banco.cadastrar(conta1);
		banco.cadastrar(conta2);
		banco.associarConta(cliente.getCpf(), conta1.getNumero());
		banco.associarConta(cliente.getCpf(), conta2.getNumero());

		banco.removerCliente(cliente.getCpf());

		assertNull(banco.procurarConta(conta1.getNumero()));
		assertNull(banco.procurarConta(conta2.getNumero()));
		assertNull(banco.procurarCliente(cliente.getCpf()));
	}

	@Test
	public void testeRemoverClienteComContasAssociadas() throws RepositorioException, ClienteNaoCadastradoException,
			ContaNaoEncontradaException, ClienteNaoPossuiContaException, ClienteJaPossuiContaException {
		RepositorioClientesArquivoBin mockRepositorioClientes = mock(RepositorioClientesArquivoBin.class);
		RepositorioContasArquivoBin mockRepositorioContas = mock(RepositorioContasArquivoBin.class);
		Banco banco = new Banco(mockRepositorioClientes, mockRepositorioContas);
		Cliente cliente = new Cliente("Ana", "123456789");
		cliente.adicionarConta("001");
		cliente.adicionarConta("002");
		when(mockRepositorioClientes.procurar(cliente.getCpf())).thenReturn(cliente);
		Mockito.doNothing().when(mockRepositorioContas).remover(Mockito.anyString());
		when(mockRepositorioClientes.remover(cliente.getCpf())).thenReturn(true);
		banco.removerCliente(cliente.getCpf());
		verify(mockRepositorioContas, Mockito.times(1)).remover("001");
		verify(mockRepositorioContas, Mockito.times(1)).remover("002");
		verify(mockRepositorioClientes, Mockito.times(1)).remover(cliente.getCpf());
	}

	@Test
	public void testeRemoverClienteNaoRemovidoComContas() throws RepositorioException, ClienteNaoCadastradoException,
            ContaNaoEncontradaException, ClienteNaoPossuiContaException, ClienteJaPossuiContaException {
		RepositorioClientesArquivoBin mockRepositorioClientes = mock(RepositorioClientesArquivoBin.class);
		RepositorioContasArquivoBin mockRepositorioContas = mock(RepositorioContasArquivoBin.class);
		Banco banco = new Banco(mockRepositorioClientes, mockRepositorioContas);

		Cliente cliente = new Cliente("Ana", "123456789");
		cliente.adicionarConta("001");
		cliente.adicionarConta("002");
		when(mockRepositorioClientes.procurar(cliente.getCpf())).thenReturn(cliente);
		when(mockRepositorioClientes.remover(cliente.getCpf())).thenReturn(false);

		Exception excecaoLançada = null;
		try {
			banco.removerCliente(cliente.getCpf());
		} catch (ClienteNaoCadastradoException e) {
			excecaoLançada = e;
		}

		assertNotNull("Exceção ClienteNaoCadastradoException esperada", excecaoLançada);
		assertTrue(excecaoLançada instanceof ClienteNaoCadastradoException);

		verify(mockRepositorioContas, Mockito.times(1)).remover("001");
		verify(mockRepositorioContas, Mockito.times(1)).remover("002");
	}

	@Test(expected = ClienteNaoCadastradoException.class)
	public void testeRemoverClienteNaoRemovido() throws RepositorioException, ClienteNaoCadastradoException, ClienteNaoPossuiContaException, ContaNaoEncontradaException {
		RepositorioClientesArquivoBin mockRepositorioClientes = mock(RepositorioClientesArquivoBin.class);
		RepositorioContasArquivoBin mockRepositorioContas = mock(RepositorioContasArquivoBin.class);
		Banco banco = new Banco(mockRepositorioClientes, mockRepositorioContas);

		Cliente cliente = new Cliente("Carlos", "111222333");
		when(mockRepositorioClientes.procurar(cliente.getCpf())).thenReturn(cliente);
		when(mockRepositorioClientes.remover(cliente.getCpf())).thenReturn(false);

		try {
			banco.removerCliente(cliente.getCpf());
		} catch (ClienteNaoCadastradoException | ContaNaoEncontradaException | ClienteNaoPossuiContaException e) {
			assertEquals("O cliente não foi encontrado no repositório após a tentativa de remoção.", e.getMessage());
			throw e;
		}
		verify(mockRepositorioContas, Mockito.times(1)).remover(Mockito.anyString());
		verify(mockRepositorioClientes, Mockito.times(1)).remover(cliente.getCpf());
	}


	@Test
	public void testeAtualizarClienteComSucesso() throws RepositorioException, AtualizacaoNaoRealizadaException {
		RepositorioClientesArquivoBin mockRepositorioClientes = mock(RepositorioClientesArquivoBin.class);
		Banco banco = new Banco(mockRepositorioClientes, new RepositorioContasArquivoBin());
		Cliente cliente = new Cliente("Carlos", "111222333");

		when(mockRepositorioClientes.atualizar(cliente)).thenReturn(true);
		banco.atualizarCliente(cliente);
		verify(mockRepositorioClientes, Mockito.times(1)).atualizar(cliente);
	}

	@Test
	public void testeAtualizarClienteFalha() throws RepositorioException {
		RepositorioClientesArquivoBin mockRepositorioClientes = mock(RepositorioClientesArquivoBin.class);
		Banco banco = new Banco(mockRepositorioClientes, new RepositorioContasArquivoBin());

		Cliente cliente = new Cliente("Carlos", "111222333");
		when(mockRepositorioClientes.atualizar(cliente)).thenReturn(false);
		Exception excecaoLançada = null;
		try {
			banco.atualizarCliente(cliente);
		} catch (AtualizacaoNaoRealizadaException e) {
			excecaoLançada = e;
		}
		assertNotNull("Exceção AtualizacaoNaoRealizadaException esperada", excecaoLançada);
		assertTrue(excecaoLançada instanceof AtualizacaoNaoRealizadaException);

		verify(mockRepositorioClientes, Mockito.times(1)).atualizar(cliente);
	}



	@Test(expected = ValorInvalidoException.class)
	public void testeDebitarValorInvalido() throws RepositorioException, SaldoInsuficienteException, ValorInvalidoException, ContaNaoEncontradaException, ContaJaCadastradaException {
		ContaAbstrata conta = new Conta("1", 100);
		banco.cadastrar(conta);
		double valorInvalido = -50;
		banco.debitar(conta, valorInvalido);
	}


	@Before
	public void setUp() {
		poupanca = mock(Poupanca.class);
		contaNaoPoupanca = mock(ContaAbstrata.class);
	}

	@Test
	public void testRenderJurosContaPoupancaExistente() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException {
		when(poupanca.getNumero()).thenReturn("12345");
		when(repositorioContas.existe(poupanca.getNumero())).thenReturn(true);

		banco.renderJuros(poupanca);

		verify(poupanca).renderJuros(0.5);
		verify(repositorioContas).atualizar(poupanca);
	}

	@Test(expected = ContaNaoEncontradaException.class)
	public void testRenderJurosContaPoupancaNaoExistente() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException {
		when(poupanca.getNumero()).thenReturn("12345");
		when(repositorioContas.existe(poupanca.getNumero())).thenReturn(false);

		banco.renderJuros(poupanca);
	}

	@Test(expected = RenderJurosPoupancaException.class)
	public void testRenderJurosContaNaoPoupanca() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException {
		banco.renderJuros(contaNaoPoupanca);
	}
}
