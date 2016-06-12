package exercicio;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;

public class JPAProdutoDAO implements ProdutoDAO{
	
/*um produto :
 * id
 * nome
 * lanceM�nimo
 * dataCadastro
 * dataVenda
 */
	public long inclui(Produto umProduto){	
		
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try
		{	// transiente - objeto novo: ainda n�o persistente   - quando o valor do atributo identificador � null
			// persistente - apos ser persistido 
			// destacado - objeto persistente n�o vinculado a um entity manager  - qd valor do atributo id � != null e o obj n�o est� sendo monitorado pelo entityManager (em.close())
			
			em = FabricaDeEntityManager.criarSessao();
			
			tx = em.getTransaction();
			
			tx.begin();
			
			//persistente - quando obj est� sendo monitorado pelo entityManager
			em.persist(umProduto);  // agenda mas n�o executa, s� executa no commit
			
			//vai no cash do entity manager primeiro
			//Produto p = em.find(Produto.class, umProduto.getId());
	/*
	 * 1 Executar: select SEQ_PRODUTO.NEXTVAL FROM SYS.DUAL;
	 * 2 Atribui o pr�ximo valor da sequence ao campo id do produto
	 * 3 faz com que objeto produto passe a ser monitorado pelo entityManager
	 * 4 Agenda a execu��o do insert(comando sql)		
	 */
			
	/*
	 * entitYManager � a primeiro n�vel do cash
	 * 
	 */
			umProduto.setNome("XXXXXXXXXXX");   // agenda mas n�o executa, s� executa no commit
			
			tx.commit(); // executa insert, update e commita
			
			return umProduto.getId();      
		} 
		catch(RuntimeException e) // exe��o do commit
		{	if (tx != null)   // falha, n�o conectou no banco...se tx for null n�o abriu a transa��o
			{	
				try
				{	tx.rollback(); // se != null abriu a transa��o ...vamos tentar o rollback....mas se commit falha rollback tem grande possibilidade de falhar
				}
				catch(RuntimeException he)  // exe��o do rollback
				{ }  // mata a exe��o
			}
			throw e;// ressuscita a exe��o
		}
		finally
		{	em.close(); // destroi pois ele � o primeiro n�vel de cash.(perigoso n�o destruir principalmente em variaveis de classe)	
		}
	}

	public void altera(Produto umProduto) throws ProdutoNaoEncontradoException
	{	EntityManager em = null;
		EntityTransaction tx = null;
		Produto produto = null;
		try
		{	
			em = FabricaDeEntityManager.criarSessao();
			tx = em.getTransaction();
			tx.begin();
			
			//Bug hibernate jpa n�o permite fazer assim....documenta��o do jpa manda retornar exe��o
			//mas ao inves ela insere o produto
			//em.merge(umProduto);
			
			produto = em.find(Produto.class, umProduto.getId(), LockModeType.PESSIMISTIC_WRITE); // lock no registro do bd
			
			if(produto == null){
				
				tx.rollback();
				throw new ProdutoNaoEncontradoException("Produto n�o encontrado");
			}
			em.merge(umProduto);

			tx.commit();
		} 
		catch(RuntimeException e){ 
			if (tx != null)
		    {   
				try
		        {	tx.rollback();
		        }
		        catch(RuntimeException he)
		        { }
		    }
		    throw e;
		}
		finally{   
			em.close();
		}
	}
		// recebe o id
	public void exclui(long numero) throws ProdutoNaoEncontradoException {	
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try	{	
			//cri a novo entity manager
			em = FabricaDeEntityManager.criarSessao();
			tx = em.getTransaction();
			tx.begin();
			
			//recupera com lock no registro(n�o pode alterar enquanto tiver lockado)
			Produto produto = em.find(Produto.class, new Long(numero), LockModeType.PESSIMISTIC_WRITE);
			
			if(produto == null){	
				tx.rollback();
				throw new ProdutoNaoEncontradoException("Produto n�o encontrado");
			}
			// jpa vai agendar delete pra produto (jpa remove = hibernate delete session.delete(produto))
			// hibernate n�o precisa recuperar o produto(persistente), pode passar um produto sem recuperar(destacado)
			// jpa tem que ser persistente(recuperar o produto com find)
			em.remove(produto);
			
			//se commitar um delete ou update em uma linha que n�o existe n�o d� erro
			tx.commit();
			
		} 
		catch(RuntimeException e)	{   
			if (tx != null){   
				try {	
					tx.rollback();
		        }catch(RuntimeException he){ 
		        	
		        }
		    }
		    throw e;
		}
		finally{   
			em.close();
		}
	}

	public Produto recuperaUmProduto(long numero) throws ProdutoNaoEncontradoException{	
		EntityManager em = null;
		
		try{	
			em = FabricaDeEntityManager.criarSessao();

			// m�todo find recebe produto .class ent�o j� sabe que vai retornar um produto...n�o precisa cash
			Produto umProduto = em.find(Produto.class, numero);  // qd recupera o produto, entity manager n�o monitora
			
			// Caracter�sticas no m�todo find():
			// 1. � gen�rico: n�o requer um cast.
			// 2. Retorna null caso a linha n�o seja encontrada no banco.

			if(umProduto == null){	
				throw new ProdutoNaoEncontradoException("Produto n�o encontrado");
			}
			return umProduto;
		} 
		finally
		{   em.close();
		}
	}

	
	public List<Produto> recuperaProdutos(){	
		EntityManager em = null;
		
		try{	
			em = FabricaDeEntityManager.criarSessao();
			
			// warning --> m�todo n�o garante que ser� list de produtos
			@SuppressWarnings("unchecked")
			List<Produto> produtos =
				em.createQuery("select p from Produto p order by p.id")
				.getResultList();
			//implementa��o do jpa fornecida pelo hibernate pode ser "from Produto p order by p.id"
			//pra retornar td da tabela

			// Retorna um List vazio caso a tabela correspondente esteja vazia.
			
			
			return produtos;
		//return null;
		} 
		finally{   
			em.close();
		}
	}
}