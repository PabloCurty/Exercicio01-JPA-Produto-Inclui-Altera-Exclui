package exercicio;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class FabricaDeEntityManager{
	// vari�vel est�tica(de classe) fica armazenado na classe(classe na mem�ria � um objeto do tipo class)
	private static FabricaDeEntityManager fabrica = null;
	// vari�vel de instancia
	private EntityManagerFactory emf = null;
	// contrutor privado chama de um m�todo est�tico p�blico -- criarSess�o
	//controle a cria��o fazendo dar new apenas uma vez -- padr�o de projeto singleton (SINGLETON)
	private FabricaDeEntityManager()
	{	
		try
		{	
			emf = Persistence.createEntityManagerFactory("exercicio");
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			System.out.println(">>>>>>>>>> Mensagem de erro: " + e.getMessage());
		}
	}

	public static EntityManager criarSessao()
	{	if (fabrica == null)
		{	fabrica = new FabricaDeEntityManager();
		}	

		return fabrica.emf.createEntityManager();
	}

	public static void fecharFabricaDeEntityManager()
	{	if (fabrica != null)
			if (fabrica.emf != null)
				fabrica.emf.close();
	}
}