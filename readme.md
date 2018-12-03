1 - Pré-Requisitos:
	
	1.1 - Na máquina servidora:
	
		- Sistema operacional Windows ou Linux. Outros sistemas operacionais para desktops como o Mac OS também podem serem utilizados, mas não há garantias de que o servidor do JCheckers irá funcionar corretamente já que não foram realizados testes em outros sistemas operacioais além dos recomendados aqui.
	
		- MySQL Server 5.6.4 ou posterior. Qualquer versão do MySQL inferior a 5.6.4 não será compatível.
		
		- JDK 8 ou superior.
		
		- Apache Tomcat 8 ou superior.
		
	1.2 - Na máquina cliente:
	
		- JRE 8 ou superior.

2 - Instalação:

	2.1 - Na máquina servidora:
	
		- Na pasta de instalação do Apache Tomcat, copie o arquivo mysql-connector-java-5.1.39-bin.jar dentro da pasta lib. Este arquivo é conector MySQL JDBC, necessário para a conexão da aplicação servidora com o servidor MySQL. Se preferir, pode usar a versão do conector JDBC de sua preferência, porém é recomendável sempre utilizar esta versão ou outra mais atualizada.
	
		- No servidor MySQL, rode o script jcheckers.sql. Ele irá criar a base de dados necessária para o servidor JCheckers.
	
		- Para sistemas Windows, crie o diretório C:\jcheckers enquanto em sistemas Linux crie o diretório /home/jcheckers. Esse será o diretório onde estará localizada a configuração do servidor JCheckers e onde serão gravados os logs. Caso deseje que este diretório esteja em um local alternativo, crie uma variável de ambiente com o nome JCHECKERS_HOME e configure seu valor para o caminho para o diretório desejado.
		
		- Copie o arquivo config.xml para o diretório de configuração anteriormente criado.
		
		- Edite o arquivo config.xml anteriormente copiado, alterando os seguintes campos:
		
			username='<seu usuário no mysql>'
			password='<sua senha no mysql>'
			
			É necessário que o usuário escolhido possua privilégios para ao menos realizar todas as operações sobre o schema jcheckers, logo recomenda-se que seja criado um usuário somente para esse fim específico.
		
		- No Tomcat, faça o deploy do arquivo JCheckersServer.war.

3 - Configuração:

	3.1 - Na máquina servidora:
	
		- No arquivo config.xml, pode-se definir a origem da base de dados alterando os campos host e port.
	
		- Para que o sistema JCheckers seja acessível de outras máquinas na sua rede local ou mesmo na internet, é necessário alterar alguns campos na tabela games da base de dados jcheckers. Para isso altere todas as entradas correspondentes a coluna host (por padrão estará como localhost) com o ip de sua máquina.
		
		- Opcionalmente podem serem configurada a porta do servidor alterando-se todas as entradas correspondentes a coluna port na tabela games.
	
	3.2 - Na máquina cliente:
	
		- Pode-se escolher a url do servidor alterando-se o campo url no arquivo config.xml que está no mesmo diretório onde se localiza o arquivo JCheckersClient.jar. Esta url aponta para qual endereço o aplicativo cliente irá realizar a conexão e efetuar o login ou o registro.
		
		- Alternativamente, pode-se executar o arquivo JCheckersClient.jar por linha de comando passando-lhe um único argumento indicando a url do servidor. Este argumento é opcional onde caso seja emitido, o aplicativo irá buscar a url dentro do arquivo config.xml.