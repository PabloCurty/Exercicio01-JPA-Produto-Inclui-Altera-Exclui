package exercicio;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class FabricaDeEntityManager{
	// variável estática(de classe) fica armazenado na classe(classe na memória é um objeto do tipo class)
	private static FabricaDeEntityManager fabrica = null;
	// variável de instancia
	private EntityManagerFactory emf = null;
	// contrutor privado chama de um método estático público -- criarSessão
	//controle a criação fazendo dar new apenas uma vez -- padrão de projeto singleton (SINGLETON)
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