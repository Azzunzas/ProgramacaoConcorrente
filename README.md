# ProgramacaoConcorrente

Repositorio criado para armazenar o andamento da Avalição A2, cujo enunciado é:

Avalição A2 (5,0). 
Implemente um código em Java que simule uma cadeia de produção de veículos, com uma 
fábrica, duas lojas de veículos e clientes que compram nestas lojas.  
No dia da defesa: Código sem Comentários. 
Trabalho funcionando vale 1,0 para o grupo todo. 
Perguntas individuais – defesa de código vale 4,0. 

I- Na fábrica existe um estoque de peças limitados a 500 unidades. Cada unidade é 
servida as estações de montagem por uma esteira, que é capaz de atender até 5 
solicitações por vez.

II- A fábrica possui quatro estações produtoras de veículos. Em cada estação 
produtora operam 5 funcionários em uma posição circular, com uma ferramenta 
adjacente em cada lado. Para produzir um carro, o funcionário necessita pegar a 
ferramenta da esquerda e da direita. Terminando a produção do carro, o 
funcionário armazena o carro para venda, em uma esteira circular com 40 posições 
de capacidade. Cada estação produtora possui um identificador único, assim como 
cada funcionário da mesa produtora. 

III- Existem 3 lojas de veículos que compram carros desta fábrica. Estas lojas são 
remotas, então não é possível executar o programa da que simula a loja na mesma 
máquina do programa que simula a fábrica. Cada loja possui uma esteira circular 
de veículos, que são comprados diretamente fábrica e oferecidos para os clientes 
comprarem. Caso a fábrica não tenha produção disponível, a loja fica em modo de 
espera, aguardando a produção de um carro.  

IV- Os clientes compram os veículos das lojas. Nesta simulação, no total existem 20 
clientes. Um cliente se comporta como uma thread e pode comprar mais de um 
carro de maneira aleatória. A escolha da loja que o cliente vai comprar o carro 
deve ser randômica para cada tentativa. Caso a loja em questão não tenha carros 
para a venda, o cliente vai entrar em espera. 

V- A fábrica deve possuir dois logs: O primeiro contendo o número sequencial do 
veículo produzido (id), a cor do veículo produzido (alternar entre as cores RGB), o 
tipo de veículo produzido (alternar entre SUV ou SEDAN), o id da estação 
produtora do veículo, o id do funcionário que produziu o veículo e a posição que o 
veículo se encontrava na esteira no momento da produção. O segundo log será 
gerado no momento de venda efetuada pela fábrica, que deve conter todas as 
informações anteriores, mais a informação de qual loja comprou o veículo e em 
qual posição da esteira da loja o veículo foi colocado. 

VI- Quando uma loja compra um veículo, todas informações da cadeia produtiva do 
veículo são repassadas como atributos do veículo. Deve-se implementar dois logs 
para cada loja de maneira similar ao log da fábrica, mas utilizando o conceito de 
loja e cliente. O cliente não tem o conceito de esteira, mas tem o conceito de 
garagem, sendo assim a garagem é um buffer que irá alocar um veículo a cada 
compra do cliente.
