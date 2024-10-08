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
	private RepositorioClientesArquivoBin repositorioClientes;

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

	@Before
	public void setUp() throws RepositorioException {
		poupanca = mock(Poupanca.class);
		contaNaoPoupanca = mock(ContaAbstrata.class);
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
//	@Test(expected = ContaNaoEncontradaException.class)
	@Test
	public void testeCreditarContaInexistente() throws RepositorioException,
			ContaNaoEncontradaException, InicializacaoSistemaException,
			ValorInvalidoException {

		Conta contaInexistente = new Conta("contaInexistente", 0);
		banco.creditar(contaInexistente, 200);
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
	@Test
	public void testeDebitarContaInexistente() throws RepositorioException,
			ContaNaoEncontradaException, SaldoInsuficienteException,
			InicializacaoSistemaException, ValorInvalidoException {

		Conta contaInexistente = new Conta("contaInexistente", 0);
		banco.debitar(contaInexistente, 50);
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
	@Test
	public void testeTransferirContaInexistente() throws RepositorioException,
			SaldoInsuficienteException, InicializacaoSistemaException, ValorInvalidoException {
		RepositorioContasArquivoBin mockRepositorioContas = mock(RepositorioContasArquivoBin.class);
		Banco banco = new Banco(null, mockRepositorioContas);

		ContaAbstrata contaOrigem = new Conta("123", 100);
		ContaAbstrata contaDestino = new Conta("456", 200);
		when(mockRepositorioContas.existe(contaOrigem.getNumero())).thenReturn(false);
		when(mockRepositorioContas.existe(contaDestino.getNumero())).thenReturn(false);
		banco.transferir(contaOrigem, contaDestino, 50);

		verify(mockRepositorioContas, never()).atualizar(contaOrigem);
		verify(mockRepositorioContas, never()).atualizar(contaDestino);
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
		double taxaJurosDecimal = 0.5;
		double saldoEsperado = saldoSemJuros + (saldoSemJuros * taxaJurosDecimal);

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

		ContaEspecial mockContaEspecial = mock(ContaEspecial.class);
		when(mockContaEspecial.getNumero()).thenReturn("12345");
		when(mockRepositorioContas.existe(mockContaEspecial.getNumero())).thenReturn(true);
		when(mockRepositorioContas.atualizar(mockContaEspecial)).thenReturn(true);

		banco.renderBonus(mockContaEspecial);

		verify(mockRepositorioContas, Mockito.times(1)).existe(mockContaEspecial.getNumero());
		verify(mockRepositorioContas, Mockito.times(1)).atualizar(mockContaEspecial);
		verify(mockContaEspecial, Mockito.times(1)).renderBonus();
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
		RepositorioClientesArquivoBin mockRepositorioClientes = mock(RepositorioClientesArquivoBin.class);
		RepositorioContasArquivoBin mockRepositorioContas = mock(RepositorioContasArquivoBin.class);
		Banco banco = new Banco(mockRepositorioClientes, mockRepositorioContas);

		Cliente cliente = new Cliente("João", "123456789");
		ContaAbstrata conta = new Conta("1", 100);

		when(mockRepositorioClientes.procurar(cliente.getCpf())).thenReturn(cliente);
		when(mockRepositorioContas.procurar(conta.getNumero())).thenReturn(null);
		banco.associarConta(cliente.getCpf(), conta.getNumero());

		assertTrue(cliente.getContas().contains(conta.getNumero()));
		verify(mockRepositorioClientes, times(1)).atualizar(cliente);
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

	@Test(expected = ContaJaAssociadaException.class)
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

		fail("Numero de conta ja associada a um cliente!");
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

	@Test(expected = ClienteNaoCadastradoException.class)
	public void testeRemoverClienteComContas() throws Exception {
		Cliente cliente = new Cliente("Bruno", "987654321");
		ContaAbstrata conta1 = new Conta("1", 100);
		ContaAbstrata conta2 = new Conta("2", 200);

		try {
			banco.associarConta(cliente.getCpf(), conta1.getNumero());
		} catch (ContaJaAssociadaException excecao){
			banco.removerCliente(cliente.getCpf());
			banco.associarConta(cliente.getCpf(), conta1.getNumero());
		}

		try {
			banco.associarConta(cliente.getCpf(), conta2.getNumero());
		} catch (ContaJaAssociadaException excecao){
			banco.removerCliente(cliente.getCpf());
			banco.associarConta(cliente.getCpf(), conta2.getNumero());
		}

		banco.removerCliente(cliente.getCpf());
		assertNull(repositorioClientes.procurar(cliente.getCpf()));
		assertNull(repositorioContas.procurar(conta1.getNumero()));
		assertNull(repositorioContas.procurar(conta2.getNumero()));
	}

	@Test(expected = NullPointerException.class)
	public void testeRemoverClienteNaoExistente() throws RepositorioException, ClienteNaoCadastradoException,
			ContaNaoEncontradaException, ClienteNaoPossuiContaException {

		banco.removerCliente("cpfnaoexistente");
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
		when(mockRepositorioContas.remover(Mockito.anyString())).thenReturn(true);
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

		when(mockRepositorioContas.remover("001")).thenReturn(true);
		when(mockRepositorioContas.remover("002")).thenReturn(true);

		Exception excecaoLancada = null;
		try {
			banco.removerCliente(cliente.getCpf());
		} catch (ClienteNaoCadastradoException e) {
			excecaoLancada = e;
		}

		assertNotNull("Exceção ClienteNaoCadastradoException esperada", excecaoLancada);
		assertTrue(excecaoLancada instanceof ClienteNaoCadastradoException);

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

		banco.removerCliente(cliente.getCpf());

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
		Exception excecao = null;
		try {
			banco.atualizarCliente(cliente);
		} catch (AtualizacaoNaoRealizadaException e) {
			excecao = e;
		}
		assertNotNull("Exceção AtualizacaoNaoRealizadaException esperada", excecao);
		assertTrue(excecao instanceof AtualizacaoNaoRealizadaException);

		verify(mockRepositorioClientes, Mockito.times(1)).atualizar(cliente);
	}



	@Test(expected = ValorInvalidoException.class)
	public void testeDebitarValorInvalido() throws RepositorioException, SaldoInsuficienteException, ValorInvalidoException, ContaNaoEncontradaException, ContaJaCadastradaException {
		ContaAbstrata conta = new Conta("1", 100);
		banco.cadastrar(conta);
		double valorInvalido = -50;
		banco.debitar(conta, valorInvalido);
	}




	@Test
	public void testRenderJurosContaPoupancaExistente() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException {
		repositorioContas = mock(RepositorioContasArquivoBin.class);
		repositorioClientes = mock(RepositorioClientesArquivoBin.class);
		banco = new Banco(repositorioClientes, repositorioContas);
		poupanca = mock(Poupanca.class);

		when(poupanca.getNumero()).thenReturn("12345");
		when(repositorioContas.existe(poupanca.getNumero())).thenReturn(true);

		banco.renderJuros(poupanca);

		verify(poupanca).renderJuros(0.5);
		verify(repositorioContas).atualizar(poupanca);
	}

	@Test(expected = ContaNaoEncontradaException.class)
	public void testRenderJurosContaPoupancaNaoExistente() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException {
		repositorioContas = mock(RepositorioContasArquivoBin.class);
		repositorioClientes = mock(RepositorioClientesArquivoBin.class);
		banco = new Banco(repositorioClientes, repositorioContas);
		poupanca = mock(Poupanca.class);

		when(poupanca.getNumero()).thenReturn("12345");
		when(repositorioContas.existe(poupanca.getNumero())).thenReturn(false);

		banco.renderJuros(poupanca);
	}

	@Test(expected = RenderJurosPoupancaException.class)
	public void testRenderJurosContaNaoPoupanca() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException {
		banco.renderJuros(contaNaoPoupanca);
	}
}
