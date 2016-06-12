package exercicio;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class FabricaDeDAOs
{	
	private static ResourceBundle prop;

	static
	{	try{	
		// resourceBundle procura arquivos de propiedades
		// nesse casso dao.properties
		prop = ResourceBundle.getBundle("dao");
		}
		catch(MissingResourceException e)
		{	System.out.println("Aquivo dao.properties n�o encontrado.");
			throw new RuntimeException(e);
		}
	}

	// a partir da vers�o 1.5 surgiram as classes genericas
	// T no nosso caso � do tipo produtoDAO (T � um class de produtoDAO)
	// <T> esse detalhe vai fazer com que o compilador n�o procure a class T
	// ou seja diz que T n�o � uma classe e sim um tipo gen�rico
	@SuppressWarnings("unchecked")
	public static <T> T getDAO(Class<T> tipo)
	{			
		T dao = null;
		String nomeDaClasse = null; 
	
		try{
			// getSimpleName = produtoDAO    getName = exercicio.produtoDAO
			// getString passa parametro(produtoDAO) e procura no arquivo prop(nessa caso � dao.properties)
			// essa referencia, achando o valor da referencia( nesse caso exercicio.JPAProdutoDAO
			nomeDaClasse = prop.getString(tipo.getSimpleName());
			//(T) Class.forName(nomeDaClasse).   at� aqui aponta para objeto tipo class
			// newInstance cria objeto do tipo JPAProdutoDAO (construtor padr�o)
			// new instance retorna object ( tem que fazer casting) casting no caso para produtoDAO
			dao = (T) Class.forName(nomeDaClasse).newInstance();
		} 
		catch (InstantiationException e)
		{	System.out.println("N�o foi poss�vel criar um objeto do tipo " + nomeDaClasse);
			throw new RuntimeException(e);
		} 
		// classe tem construtor mas contrutor � privado
		catch (IllegalAccessException e)
		{	System.out.println("N�o foi poss�vel criar um objeto do tipo " + nomeDaClasse);
			throw new RuntimeException(e);
		} 
		// n�o encontra classe(escreve errado)
		// comum no maven, pode rodar por anos mas em algum momento entra em caminhos que o maven n�o resolve
		catch (ClassNotFoundException e)
		{	System.out.println("Classe " + nomeDaClasse + " n�o encontrada");
			throw new RuntimeException(e);
		}
		// n�o encontra valor no dao.properties
		catch(MissingResourceException e)
		{	System.out.println("Chave " + tipo + " n�o encontrada em dao.properties");
			throw new RuntimeException(e);
		}
		
		return dao;
	}
}
