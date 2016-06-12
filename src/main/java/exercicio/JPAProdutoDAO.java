package exercicio;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;

public class JPAProdutoDAO implements ProdutoDAO{
	
/*um produto :
 * id
 * nome
 * lanceMínimo
 * dataCadastro
 * dataVenda
 */
	public long inclui(Produto umProduto){	
		
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try
		{	// transiente - objeto novo: ainda não persistente   - quando o valor do atributo identificador é null
			// persistente - apos ser persistido 
			// destacado - objeto persistente não vinculado a um entity manager  - qd valor do atributo id é != null e o obj não está sendo monitorado pelo entityManager (em.close())
			
			em = FabricaDeEntityManager.criarSessao();
			
			tx = em.getTransaction();
			
			tx.begin();
			
			//persistente - quando obj está sendo monitorado pelo entityManager
			em.persist(umProduto);  // agenda mas não executa, só executa no commit
			
			//vai no cash do entity manager primeiro
			//Produto p = em.find(Produto.class, umProduto.getId());
	/*
	 * 1 Executar: select SEQ_PRODUTO.NEXTVAL FROM SYS.DUAL;
	 * 2 Atribui o próximo valor da sequence ao campo id do produto
	 * 3 faz com que objeto produto passe a ser monitorado pelo entityManager
	 * 4 Agenda a execução do insert(comando sql)		
	 */
			
	/*
	 * entitYManager é a primeiro nível do cash
	 * 
	 */
			umProduto.setNome("XXXXXXXXXXX");   // agenda mas não executa, só executa no commit
			
			tx.commit(); // executa insert, update e commita
			
			return umProduto.getId();      
		} 
		catch(RuntimeException e) // exeção do commit
		{	if (tx != null)   // falha, não conectou no banco...se tx for null não abriu a transação
			{	
				try
				{	tx.rollback(); // se != null abriu a transação ...vamos tentar o rollback....mas se commit falha rollback tem grande possibilidade de falhar
				}
				catch(RuntimeException he)  // exeção do rollback
				{ }  // mata a exeção
			}
			throw e;// ressuscita a exeção
		}
		finally
		{	em.close(); // destroi pois ele é o primeiro nível de cash.(perigoso não destruir principalmente em variaveis de classe)	
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
			
			//Bug hibernate jpa não permite fazer assim....documentação do jpa manda retornar exeção
			//mas ao inves ela insere o produto
			//em.merge(umProduto);
			
			produto = em.find(Produto.class, umProduto.getId(), LockModeType.PESSIMISTIC_WRITE); // lock no registro do bd
			
			if(produto == null){
				
				tx.rollback();
				throw new ProdutoNaoEncontradoException("Produto não encontrado");
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
			
			//recupera com lock no registro(não pode alterar enquanto tiver lockado)
			Produto produto = em.find(Produto.class, new Long(numero), LockModeType.PESSIMISTIC_WRITE);
			
			if(produto == null){	
				tx.rollback();
				throw new ProdutoNaoEncontradoException("Produto não encontrado");
			}
			// jpa vai agendar delete pra produto (jpa remove = hibernate delete session.delete(produto))
			// hibernate não precisa recuperar o produto(persistente), pode passar um produto sem recuperar(destacado)
			// jpa tem que ser persistente(recuperar o produto com find)
			em.remove(produto);
			
			//se commitar um delete ou update em uma linha que não existe não dá erro
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

			// método find recebe produto .class então já sabe que vai retornar um produto...não precisa cash
			Produto umProduto = em.find(Produto.class, numero);  // qd recupera o produto, entity manager não monitora
			
			// Características no método find():
			// 1. É genérico: não requer um cast.
			// 2. Retorna null caso a linha não seja encontrada no banco.

			if(umProduto == null){	
				throw new ProdutoNaoEncontradoException("Produto não encontrado");
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
			
			// warning --> método não garante que será list de produtos
			@SuppressWarnings("unchecked")
			List<Produto> produtos =
				em.createQuery("select p from Produto p order by p.id")
				.getResultList();
			//implementação do jpa fornecida pelo hibernate pode ser "from Produto p order by p.id"
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