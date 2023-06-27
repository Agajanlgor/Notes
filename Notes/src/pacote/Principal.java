package pacote;

import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.*;
import java.sql.*;

public class Principal extends JFrame implements ActionListener
{
    private static final long serialVersionUID = 1L;
    
    Calendar calendario;
    SimpleDateFormat formatoHora;
    SimpleDateFormat formatoData;
    Timer cronometro;
    Timer relogio;
    File arquivo;
    
    Connection conexao = null;
    ResultSet conjuntoResultados;
    
    boolean iniciado = false;
    int tempoDecorrido = 0;
    int segundo = 0;
    int minuto = 0;
    
    String tempoSegundo = String.format("%02d", segundo);
    String tempoMinuto = String.format("%02d", minuto);
    
    JLabel rotulo1;
    JLabel rotulo2;
    JLabel rotulo3;
    JButton botao1;
    JButton botao2;
    JButton botao3;
    JButton botao4;
    JButton botao5;
    JEditorPane painelEditor;
    JTable tabela;
    DefaultTableModel modeloTabela;
    JScrollPane painelRolagem1;
    JScrollPane painelRolagem2;
    
    Font fonte = new Font("Tahoma", Font.PLAIN, 18);

    public static void main(String[] args)
    {
    	SwingUtilities.invokeLater(() ->
    	{
            Principal quadro = new Principal();
        });
    }

    public Principal()
    {
        setTitle("Notes");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setVisible(true);

        botao1 = new JButton("Cadastrar dado");
        botao1.setBounds(10, 30, 156, 50);
        botao1.setFont(fonte);
        getContentPane().add(botao1);
        botao1.addActionListener(this);

        botao2 = new JButton("Atualizar dado");
        botao2.setBounds(10, 147, 156, 50);
        botao2.setFont(fonte);
        getContentPane().add(botao2);
        botao2.addActionListener(this);

        botao3 = new JButton("Excluir dado");
        botao3.setBounds(10, 269, 156, 50);
        botao3.setFont(fonte);
        getContentPane().add(botao3);
        botao3.addActionListener(this);

        rotulo1 = new JLabel("00:00:00");
        rotulo1.setBounds(10, 400, 116, 30);
        rotulo1.setFont(fonte);
        getContentPane().add(rotulo1);
        formatoHora = new SimpleDateFormat("kk:mm:ss");

        rotulo2 = new JLabel();
        rotulo2.setText("00 de Setembro de 0000");
        rotulo2.setBounds(147, 400, 197, 30);
        rotulo2.setFont(fonte);
        getContentPane().add(rotulo2);
        formatoData = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");

        rotulo3 = new JLabel();
        rotulo3.setBounds(116, 525, 175, 53);
        rotulo3.setFont(new Font("Tahoma", Font.PLAIN, 50));
        getContentPane().add(rotulo3);
        rotulo3.setText(tempoMinuto + ":" + tempoSegundo);
        
        cronometro = new Timer(1000, this);
        cronometro.setInitialDelay(0);

        botao4 = new JButton("Começar");
        botao4.setBounds(10, 622, 168, 50);
        botao4.setFont(fonte);
        getContentPane().add(botao4);
        botao4.addActionListener(this);

        botao5 = new JButton("Zerar");
        botao5.setBounds(176, 622, 168, 50);
        botao5.setFont(fonte);
        getContentPane().add(botao5);
        botao5.addActionListener(this);
        
        painelRolagem1 = new JScrollPane();
        painelRolagem1.setBounds(176, 10, 1078, 325);
        getContentPane().add(painelRolagem1);
        String[] diasSemana = {"Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira"};
        modeloTabela = new DefaultTableModel(37, diasSemana.length);
        modeloTabela.setColumnIdentifiers(diasSemana);
        tabela = new JTable(modeloTabela);
        painelRolagem1.setViewportView(tabela);


        painelRolagem2 = new JScrollPane();
        painelRolagem2.setBounds(354, 346, 900, 326);
        getContentPane().add(painelRolagem2);
        painelEditor = new JEditorPane();
        painelEditor.setFont(new Font("Tahoma", Font.PLAIN, 11));
        painelRolagem2.setViewportView(painelEditor);

        painelEditor.addKeyListener(new KeyAdapter()
        {
        	public void keyReleased(KeyEvent e)
        	{
                salvarAnotacao();
            }
        });

        String diretorio = System.getProperty("user.home") + File.separator + "anotacao.txt";
        arquivo = new File(diretorio);

        if (arquivo.exists())
        {
            try
            {
                FileReader leitorArquivos = new FileReader(arquivo);
                painelEditor.read(leitorArquivos, null);
                leitorArquivos.close();
            }
            catch (IOException e)
            {
                System.out.print(e);
            }
        }

        horario();
        conexaoBancodados();
        carregarDados();
    }

    void horario()
    {
        ActionListener ouvinteAcao = new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                String hora = formatoHora.format(Calendar.getInstance().getTime());
                rotulo1.setText(hora);
                String data = formatoData.format(Calendar.getInstance().getTime());
                rotulo2.setText(data);
            }
        };
        relogio = new Timer(500, ouvinteAcao);
        relogio.start();
    }

    void conexaoBancodados()
    {
        try
        {
            conexao = DriverManager.getConnection("jdbc:mysql://localhost:3306/banco_de_dados", "root", "");
        }
        catch (SQLException e)
        {
        	JOptionPane.showOptionDialog(this, "Não foi possível efetuar a conexão com o banco de dados.", "Erro", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            System.out.print(e);
        }
    }
    
    void carregarDados()
    {
        try
        {
            String selecionar = "SELECT * FROM tabela";
            PreparedStatement declaracaoSelecionar = conexao.prepareStatement(selecionar);
            conjuntoResultados = declaracaoSelecionar.executeQuery();

            while (conjuntoResultados.next())
            {
                int linha = conjuntoResultados.getInt("linha");
                int coluna = conjuntoResultados.getInt("coluna");
                String texto = conjuntoResultados.getString("texto");
                modeloTabela.setValueAt(texto, linha, coluna);
            }
            conjuntoResultados.close();
            declaracaoSelecionar.close();
        }
        catch (Exception e)
        {
            JOptionPane.showOptionDialog(this, "Não foi possível carregar os dados da tabela", "Erro", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            System.out.print(e);
        }
    }


    void cadastrar(int linha, int coluna, String texto)
    {
        try
        {
            String selecionar = "SELECT * FROM tabela WHERE linha = ? AND coluna = ?";
            PreparedStatement declaracaoSelecionar = conexao.prepareStatement(selecionar);
            declaracaoSelecionar.setInt(1, linha);
            declaracaoSelecionar.setInt(2, coluna);
            conjuntoResultados = declaracaoSelecionar.executeQuery();
            if (conjuntoResultados.next())
            {
            	JOptionPane.showOptionDialog(this, "Já existe um dado nesta célula.", "Alerta", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            	declaracaoSelecionar.close();
                conjuntoResultados.close();
            }
            else
            {
            	String cadastro = JOptionPane.showInputDialog(this, "Digite a informação:");
            	String inserir = "INSERT INTO tabela (linha, coluna, texto) VALUES (?, ?, ?)";
            	PreparedStatement declaracaoInserir = conexao.prepareStatement(inserir);
            	declaracaoInserir.setInt(1, linha);
            	declaracaoInserir.setInt(2, coluna);
            	declaracaoInserir.setString(3, cadastro);
            	declaracaoInserir.executeUpdate();
            	declaracaoInserir.close();
            	carregarDados();
            }
            declaracaoSelecionar.close();
            conjuntoResultados.close();
            JOptionPane.showOptionDialog(this, "Dado salvo com sucesso.", "", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
        }
        catch (Exception e)
        {
        	JOptionPane.showOptionDialog(this, "Não foi possível salvar o dado.", "Erro", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            System.out.print(e);
        }
    }
    
    void atualizar(int linha, int coluna, String novoTexto)
    {
        try
        {
            String atualizar = "UPDATE tabela SET texto = ? WHERE linha = ? AND coluna = ?";
            PreparedStatement declaracaoAtualizar = conexao.prepareStatement(atualizar);
            declaracaoAtualizar.setString(1, novoTexto);
            declaracaoAtualizar.setInt(2, linha);
            declaracaoAtualizar.setInt(3, coluna);
            declaracaoAtualizar.executeUpdate();
            declaracaoAtualizar.close();
            modeloTabela.setValueAt(novoTexto, linha, coluna);
            JOptionPane.showOptionDialog(this, "Dado atualizado com sucesso.", "", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
        }
        catch (Exception e)
        {
        	JOptionPane.showOptionDialog(this, "Não foi possível atualizar o dado.", "Erro", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            System.out.print(e);
        }
    }

    
    void excluir(int linha, int coluna)
    {
        try
        {
            String deletar = "DELETE FROM tabela WHERE linha = ? AND coluna = ?";
            PreparedStatement declaracaoDeletar = conexao.prepareStatement(deletar);
            declaracaoDeletar.setInt(1, linha);
            declaracaoDeletar.setInt(2, coluna);
            declaracaoDeletar.executeUpdate();
            declaracaoDeletar.close();
            JOptionPane.showOptionDialog(this, "Dado excluído com sucesso.", "", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            carregarDados();
            modeloTabela.setValueAt("", linha, coluna);
        }
        catch (Exception e)
        {
            JOptionPane.showOptionDialog(this, "Não foi possível excluir o dado.", "Erro", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            System.out.print(e);
        }
    }


    void salvarAnotacao()
    {
        try
        {
            FileWriter gravadorArquivo = new FileWriter(arquivo);
            painelEditor.write(gravadorArquivo);
            gravadorArquivo.close();
        }
        catch (IOException e)
        {
            System.out.print(e);
        }
    }

    public void actionPerformed(ActionEvent e)
    {

        if (e.getSource() == botao1)
        {
            int linhaSelecionada = tabela.getSelectedRow();
            int colunaSelecionada = tabela.getSelectedColumn();
            if (linhaSelecionada != -1 && colunaSelecionada != -1)
            {
                String cadastro = "?";
                if (cadastro != null && !cadastro.isEmpty())
                {
                    cadastrar(linhaSelecionada, colunaSelecionada, cadastro);
                }
            }
        }
        
        if (e.getSource() == botao2)
        {
            int linhaSelecionada = tabela.getSelectedRow();
            int colunaSelecionada = tabela.getSelectedColumn();
            if (linhaSelecionada != -1 && colunaSelecionada != -1)
            {
                String cadastro = JOptionPane.showInputDialog(this, "Atualize a informação:");
                if (cadastro != null && !cadastro.isEmpty())
                {
                	atualizar(linhaSelecionada, colunaSelecionada, cadastro);
                }
            }
        }
        
        if (e.getSource() == botao3)
        {
            int linhaSelecionada = tabela.getSelectedRow();
            int colunaSelecionada = tabela.getSelectedColumn();
            if (linhaSelecionada != -1 && colunaSelecionada != -1)
            {
                int interio = JOptionPane.showConfirmDialog(this, "Deseja excluir esse dado?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (interio == JOptionPane.YES_OPTION)
                {
                	excluir(linhaSelecionada, colunaSelecionada);
                }
            }
        }

        if (e.getSource() == botao4)
        {
            if (!iniciado)
            {
                cronometro.start();
                iniciado = true;
                botao4.setText("Pausar");
            }
            else
            {
                cronometro.stop();
                iniciado = false;
                botao4.setText("Continuar");
            }
        }
        if (e.getSource() == botao5)
        {
            cronometro.stop();
            iniciado = false;
            tempoDecorrido = 0;
            segundo = 0;
            minuto = 0;
            tempoSegundo = String.format("%02d", segundo);
            tempoMinuto = String.format("%02d", minuto);
            rotulo3.setText(tempoMinuto + ":" + tempoSegundo);
            botao4.setText("Começar");
        }
        if (e.getSource() == cronometro)
        {
            tempoDecorrido++;
            segundo = tempoDecorrido % 60;
            minuto = tempoDecorrido / 60;
            tempoSegundo = String.format("%02d", segundo);
            tempoMinuto = String.format("%02d", minuto);
            rotulo3.setText(tempoMinuto + ":" + tempoSegundo);
        }
    }
}