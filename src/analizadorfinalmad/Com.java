package analizadorfinalmad;

import compilerTools.Directory;
import compilerTools.Functions;
import compilerTools.Token;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static javax.management.Query.value;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;



public class Com extends javax.swing.JFrame {

   //tabla
    DefaultTableModel modelo;
     DefaultTableModel modelo2;
      DefaultTableModel modelo3;
       DefaultTableModel modelo4;
    
    private Directory directorio;
    //Cosas
    public ArrayList<Token> tokens;
    List<String> tokenValues = new ArrayList<>();
    java.util.Stack<String> pila = new java.util.Stack<String>();
    java.util.Stack<String> pilaaux = new java.util.Stack<>(); //Pila auxiliar
    java.util.Stack<Integer> PilaSemantica = new java.util.Stack<>();
    java.util.Stack<String> PilaOperadores = new java.util.Stack<>();
    java.util.Stack<String> ExpPosfijo = new java.util.Stack<>();
    

    int renglones = 0, columnas = 0;
    private DefaultTableModel modeloTabla;
    //Variables que uso para contar cosas NO SON MUY NECESARIAS

    private String title;
    private Timer timerKeyReleased;
    int valorAnterior = 1;
    int variable = 1;
    int errorFlag = 0; // Bandera para marcar la presencia de errores
    Boolean banP = true; //Bandera para ver si ya se inicio la Pila
    
    
    private static final Map<String, List<String>> PRODUCTIONS = new HashMap<>();
    private static final List<String> lexemas = new ArrayList<String>();

   static {
    PRODUCTIONS.put("P0", Arrays.asList("P'", "P"));
    PRODUCTIONS.put("P1", Arrays.asList("P", "Tipo", "id", "V"));
    PRODUCTIONS.put("P2", Arrays.asList("P", "A"));
    PRODUCTIONS.put("P3", Arrays.asList("Tipo", "int"));
    PRODUCTIONS.put("P4", Arrays.asList("Tipo", "float"));
    PRODUCTIONS.put("P5", Arrays.asList("Tipo", "char"));
    PRODUCTIONS.put("P6", Arrays.asList("V", ",", "id", "V"));
    PRODUCTIONS.put("P7", Arrays.asList("V", ";", "P"));
    PRODUCTIONS.put("P8", Arrays.asList("A", "id", "=", "Exp", ";"));
    PRODUCTIONS.put("P9", Arrays.asList("Exp", "+", "Term", "E"));
    PRODUCTIONS.put("P10", Arrays.asList("Exp", "-", "Term", "E"));
    PRODUCTIONS.put("P11", Arrays.asList("Exp", "Term", "E"));
    PRODUCTIONS.put("P12", Arrays.asList("E", "+", "Term", "E"));
    PRODUCTIONS.put("P13", Arrays.asList("E", "-", "Term", "E"));
    PRODUCTIONS.put("P14", Arrays.asList("E", "€"));
    PRODUCTIONS.put("P15", Arrays.asList("Term", "F", "T"));
    PRODUCTIONS.put("P16", Arrays.asList("T", "*", "F", "T"));
    PRODUCTIONS.put("P17", Arrays.asList("T", "/", "F", "T"));
    PRODUCTIONS.put("P18", Arrays.asList("T", "€"));
    PRODUCTIONS.put("P19", Arrays.asList("F", "id"));
    PRODUCTIONS.put("P20", Arrays.asList("F", "num"));
    PRODUCTIONS.put("P21", Arrays.asList("F", "(", "Exp", ")"));
}

    private Stack<String> stack = new Stack<>();
     
     private String resultado = "";
    private String error = "";
    private int is;
    private String estado;
    private String accion;
    private Stack<String> pilaux;
    private int cont = 0;
    private SymbolTable tabladeSimbolos = new SymbolTable();

    //esto es como un guardado
    String encabezadosRenglones[] = {"q0", "q1", "q2", "q3", "q4", "q5", "q6", "q7", "q8", "q9", "q10", "q11", "q12", "q13", "q14", "q15", "q16", "q17", "q18","q19","q20","q21","q22","q23","q24","q25","q26","q27","q28","q29","q30","q31","q32","q33","q34","q35","q36","q37","q38","q39","q40","q41","q42","q43","q44"};
    String encabezadosColumnas[] = {"id", "num", "int", "float", "char", ",", ";", "+", "-", "*", "/", "(", ")", "=", "$", "P", "Tipo", "V", "A", "Exp", "E", "Term", "T", "F"};
     
    // Tabla de respaldo por si se desmadra
    String matriz[][] = {
      //#"id", "num", "int", "float", "char", ",", ";",  "+",  "-",  "*",  "/",  "(", ")",  "=", "$", "P", "Tipo", "V", "A", "Exp", "E", "Term", "T", "F"
    {"q7",  "",   "q4",   "q5",    "q6",  "",  "",   "",   "",   "",   "",   "",  "",   "",  "", "q1", "q2",    "","q3",  "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "", "P0", "",   "",     "", "",   "",   "",    "",   "",  "" },
    {"q8",  "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "", "P2", "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"P3",  "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"P4",  "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"P5",  "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",  "q9", "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "", "q11","q12", "",   "",   "",   "",   "",  "",   "", "P1",  "",   "",  "q10","",   "",   "",    "",   "",  ""},
    {"q18","q19",  "",     "",      "",   "",  "",  "q14","q15", "",   "", "q20", "",   "",  "",  "",   "",     "", "",   "q13","",    "q16", "","q17"},
    {"P1",  "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "", "P1",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"q21", "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"q7",  "",   "q4",    "q5",    "q6",   "",  "",   "",   "",   "",   "",   "",  "",   "","P7","q22","q2",    "","q3",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "","q23",  "",   "",   "",   "",   "",  "",   "", "P8", "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"q18","q19",  "",     "",      "",   "",  "",   "",   "",   "",   "", "q20", "",   "",  "",  "",   "",     "", "",   "",   "",   "q24", "", "q17"},
    {"q18","q19",  "",     "",      "",   "",  "",   "",   "",   "",   "", "q20", "",   "",  "",  "",   "",     "", "",   "",   "",   "q25", "", "q17"},
    {"",    "",    "",     "",      "",   "", "P14","q27","q28", "",   "",   "", "P14", "",  "",  "",   "",     "", "",   "", "q26",   "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P18","P18","P18","q30","q31", "", "P18", "",  "",  "",   "",     "", "",   "",   "",    "", "q29", ""},
    {"",    "",    "",     "",      "",   "", "P19","P19","P19","P19","P19", "", "P19", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P20","P20","P20","P20","P20", "", "P20", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"q18","q19",  "",     "",      "",   "",  "",  "q14","q15", "",   "", "q20", "",   "",  "",  "",   "",     "", "",   "q32", "",   "q16","q29","q17"},
    {"",    "",    "",     "",      "", "q11","q12", "",   "",   "",   "",   "",  "",   "",  "",  "",   "",   "q33","",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "", "P7", "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "", "P8", "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P14","q27","q28", "",   "",   "", "P14", "",  "",  "",   "",     "", "",   "", "q35",   "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P14","q27","q28", "",   "",   "", "P14", "",  "",  "",   "",     "", "",   "", "q36",   "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P11", "",   "",   "",   "",   "", "P11", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"q18","q19",  "",     "",      "",   "",  "",   "",   "",   "",   "", "q20", "",   "",  "",  "",   "",     "", "",   "",   "",  "q36",  "q29", "q17"},
    {"q18","q19",  "",     "",      "",   "",  "",   "",   "",   "",   "", "q20", "",   "",  "",  "",   "",     "", "",   "",   "",  "q37",  "", "q17"},
    {"",    "",    "",     "",      "",   "", "P15","P15","P15", "",   "",   "", "P15", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"q18","q19",  "",     "",      "",   "",  "",   "",   "",   "",   "", "q20", "",   "",  "",  "",   "",     "", "",   "",   "",    "",   "", "q38"},
    {"q18","q19",  "",     "",      "",   "",  "",   "",   "",   "",   "", "q20", "",   "",  "",  "",   "",     "", "",   "",   "",    "",   "", "q40"},
    {"",    "",    "",     "",      "",   "","error",   "",   "",   "",   "",   "", "q40", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "",  "",   "",   "",   "",   "",   "",  "",   "", "P6", "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P9",  "",   "",   "",   "",   "", "P9",  "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P10", "",   "",   "",   "",   "", "P10", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P14","q27","q28", "",   "",   "", "P14", "",  "",  "",   "",     "", "",   "", "q41",   "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P14","P17","q28", "",   "",   "", "P14", "",  "",  "",   "",     "", "",   "", "q42",   "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P18","P18","P18","q30","q31", "", "P18", "",  "",  "",   "",     "", "",   "",   "",    "", "q44", ""},
    {"",    "",    "",     "",      "",   "", "P18","P18","P18","q30","q31", "", "P18", "",  "",  "",   "",     "", "",   "",   "",    "", "q45", ""},
    {"",    "",    "",     "",      "",   "", "P21","P21","P21","P21","P21", "", "P21", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P12", "",   "",   "",   "",   "", "P12", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P13", "",   "",   "",   "",   "", "P12", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P16","P16","P16", "",   "",   "", "P16", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""},
    {"",    "",    "",     "",      "",   "", "P17","P17","P17", "",   "",   "", "P17", "",  "",  "",   "",     "", "",   "",   "",    "",   "",  ""}
    };
    
    
    
    NumeroLinea numeroLinea;

    public Com() {
        encabezadosRenglones = new String[]{"q0", "q1", "q2", "q3", "q4", "q5", "q6", "q7", "q8", "q9", "q10", "q11", "q12", "q13", "q14", "q15", "q16", "q17", "q18","q19","q20","q21","q22","q23","q24","q25","q26","q27","q28","q29","q30","q31","q32","q33","q34","q35","q36","q37","q38","q39","q40","q41","q42","q43","q44"};
        encabezadosColumnas = new String[] {"id", "num", "int", "float", "char", ",", ";", "+", "-", "*", "/", "(", ")", "=", "$", "P", "Tipo", "V", "A", "Exp", "E", "Term", "T", "F"};
        initComponents();
          colors();
        Inicializa();
        cosasVisuales();
        Inicial();
        
      
        this.tokens = new ArrayList<>();


       

    }

    private void Inicial() {

        title = "Compiler";

        // Coloca la ventana en el centro de la pantalla.
        setLocationRelativeTo(null);

        //Numero de linea
        numeroLinea = new NumeroLinea(escritura);
        jScrollPane1.setRowHeaderView(numeroLinea);

        // Crea un nuevo objeto Directory.
        // "this" se refiere al objeto actual, "escritura" es una referencia a algún tipo de componente de texto, "title" es el título y ".ldas"  la extensión de un tipo de archivo.
        directorio = new Directory(this, escritura, title, ".mad");

        // Agrega un escuchador de eventos a la ventana. Este escuchador responde a cuando la ventana se está cerrando.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Antes de que la ventana se cierre, se llama a la función Exit() del objeto directorio.
                directorio.Exit();
                // Después se cierra la aplicación.
                System.exit(0);
            }
        });

        // Se llama a una función que parece establecer un número de línea en el componente de texto "escritura".
        //Functions.setLineNumberOnJTextComponent(escritura);
        // Inicializa un nuevo temporizador que se detiene después de 0.3 segundos.
        timerKeyReleased = new Timer((int) (1000 * 0.3), (ActionEvent e) -> {
            timerKeyReleased.stop();
        });

        // Llama a una función que parece insertar un asterisco en el nombre. 
        // La función también reinicia el temporizador cuando se libera una tecla.
        Functions.insertAsteriskInName(this, escritura, () -> {
            timerKeyReleased.restart();
        });

        // Configura un componente de texto "escritura" para autocompletar basado en un conjunto de cadenas vacío.
        // La función también reinicia el temporizador cuando se libera una tecla.
    }

    public void Inicializa() {
        tokens = new ArrayList<>();

    }

    private void analisisLexico() {
        lexemas.clear();
        Lexer lexer = null;// Creamos un objeto lexer
        try {
            File sourceCodeFile = new File("code.encrypter");// Creamos un nuevo archivo llamado "code.encrypter" 

            FileOutputStream fileOutputStream = new FileOutputStream(sourceCodeFile);// Creamos un flujo de salida para escribir datos al archivo
            byte[] bytesOfText = escritura.getText().getBytes();// Obtenemos el texto de la variable escritura y lo convertimos a bytes      

            fileOutputStream.write(bytesOfText); // Escribimos los bytes al archivo

            BufferedReader fileInputReader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceCodeFile), "UTF8"));// Creamos un flujo de entrada para leer datos del archivo,

            lexer = new Lexer(fileInputReader);// Inicializamos el lexer con el contenido del archivo
            String tipodato = "";
            while (true) { // Iteramos a través de cada token producido por el lexer hasta que no haya más
                Token token = lexer.yylex();
                if (token == null) {
                    break;
                }
                // Añadimos cada token a la lista de tokens
                tokens.add(token);
                if(token.getLexicalComp() == "id"){
                    tabladeSimbolos.insertar(token.getLexeme(),"","");
                    tabladeSimbolos.imprimir();
                }
            }
        } catch (FileNotFoundException ex) {
            // En caso de que el archivo no pueda ser encontrado, se muestra un mensaje de error
            System.out.println("El archivo no pudo ser encontrado... " + ex.getMessage());
        } catch (IOException ex) {
            // En caso de un error de entrada/salida, se muestra un mensaje de error
            System.out.println("Error al escribir en el archivo... " + ex.getMessage());
        }
    }

    private void analisisLexicoEerr() {

        // Recorrer todos los tokens
        for (int i = 0; i < tokens.size(); i++) {

            // Si el token es de tipo ERROR
            if ("ERROR".equals(tokens.get(i).getLexicalComp())) {

                // En función del tipo de error, añadir el mensaje correspondiente
                String errorMsg = "Error Lexico linea " + tokens.get(i).getLine() + ": token [ " + tokens.get(i).getLexeme() + " ] ";
                if (tokens.get(i).getLexeme().matches("[\"]")) {
                    errorMsg += "se esperaba comilla doble de cierre ";
                    errorFlag = 1;
                } else if (tokens.get(i).getLexeme().matches("[\']")) {
                    errorMsg += "se esperaba comilla simple de cierre ";
                    errorFlag = 1;
                } else if (tokens.get(i).getLexeme().matches("'[^']*'")) {
                    errorMsg += "se esperaba solo un caracter entre las comillas simples ";
                    errorFlag = 1;
                } else {
                    errorMsg += "no es valido ";
                    errorFlag = 1;
                }

                // Añadir la línea y columna del error
                errorMsg += "[" + tokens.get(i).getLine() + ", " + tokens.get(i).getColumn() + "]\n";

                // Actualizar el contenido del campo mensajes
                mensajes.setText(mensajes.getText() + errorMsg);
            }
            for (Token token : tokens) {
            tokenValues.add(token.getLexicalComp()); // Asumiendo que getLexicalComp() devuelve el valor del token
        }
        }
    }

   

    boolean banpr = true;
      private List<String> input;
      
      String possibleSymbols = "";
      String PrevioSymbol2 = "";
    String PrevioSymbol = "";
    String tipodato, action;

    public void resetParserState() {
    stack.clear();
    PilaCodigo.clear();
    PilaSemantica.clear();
    PilaOperadores.clear();
    ExpPosfijo.clear();
    cont = 0;
    PrevioSymbol = "";
    PrevioSymbol2 = "";
    action = "";
    tipodato="";
    contador =0;
    V1=0;
    V2=0;
    lexemaAnterior="";
    anteriorTipo="";
    Tipo="";
    resultadoSemantico="";
    tipo= 5;
    
    // Reinicia todas las variables de estado según sea necesario
}
    
    
 public void parse() {
       stack.push("$");
        stack.push("q0");
     
     modelo = (DefaultTableModel) this.PilaSin.getModel();
     modelo2 = (DefaultTableModel) this.PilaSem.getModel();
     modelo3 = (DefaultTableModel) this.PilaOp.getModel();
     modelo4 = (DefaultTableModel) this.CodigoInt.getModel();
     
        modelo.setRowCount(0); // Limpia la tabla asociada al modelo
        modelo2.setRowCount(0); // Limpia la segunda tabla asociada al segundo modelo
        modelo3.setRowCount(0); // Limpia la tercera tabla asociada al tercer modelo
        modelo4.setRowCount(0); // Limpia la cuarta tabla asociada al cuarto modelo
     
    String lexicoText = lexico.getText()+"$"+"$";
    String firstSymbol;
    StringTokenizer tokenizer = new StringTokenizer(lexicoText, " ,;()=+-*/$", true);
    List<String> input = new ArrayList<>();
     String inputSymbol ="";
     String PrevioSymbol;
     boolean isReducing = false;
    while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken().trim();
        if (!token.isEmpty()) {
            input.add(token);
        }
   }
    //System.out.println("Contenido de input: " + input);
    Iterator<String> inputIterator = input.iterator();
     PrevioSymbol = "";
     String type = "";
     String lex = "";
    while (!stack.isEmpty() && inputIterator.hasNext()) {
        System.out.println("Pila: " + stack);
        String state = stack.peek();
        if (isReducing) {
            inputSymbol = PrevioSymbol;
            isReducing = false;
        } else {
            int i;
            for (i = 0; i < lexemas.size(); i++) {
                if (i == cont) {
                    break;
                }
                if (TipoaDato.contains(lexemas.get(i))){
                    tipodato = lexemas.get(i);
                }
            }
            if (i == lexemas.size()){
                lex = lexemas.get(i-1);
            }else{
                lex = lexemas.get(i);
            }
            
            // semantico
            if (inputIterator.hasNext()) { // Check if there are more elements in the inputIterator
                inputSymbol = inputIterator.next();
                if (i < lexemas.size()) { // Check if i is within bounds
                    Semantico(lex,inputSymbol,action);
                }
                cont += 1;
            } else {
                Semantico("$",PrevioSymbol2,action);
                inputSymbol = "$"; // Add end of input symbol
            }
        }
        //System.out.println("Contenido de PrevioSymbol: " + PrevioSymbol);
        //System.out.println("Contenido de inputSymbol: " + inputSymbol);
        //System.out.println("Contenido de inputIterator: " + inputIterator);
        // Consulta la tabla de análisis para obtener la acción
        action = getAction(state, inputSymbol);
        //System.out.println("Pila despues del getaction: " + stack);
        if (inputSymbol.equals("id")){
            if (TipoaDato.contains(PrevioSymbol2)) {
                type = PrevioSymbol2;
                tabladeSimbolos.insertar(lex,"",type);
                tabladeSimbolos.imprimir();
            }
            if(PrevioSymbol2.equals(",") && TipoaDato.contains(type)){
                tabladeSimbolos.insertar(lex,"",type);
                tabladeSimbolos.imprimir();
            }
        }
        if (action.equals("P0")) {
            // Si la acción es "P0", cambia la acción a "Accept" y cierra el bucle
            action = "Accept";
        }
        if (action.equals("P8")){
            Semantico(lex, inputSymbol, action);
        }
        

if (action.startsWith("q")) {
    /*
    if (action.equals("q21")) {
            // q11 asignar al id su tipo de dato
            tabladeSimbolos.insertar(lex, "", type);
            tabladeSimbolos.imprimir();
        }*/
    // Realiza un desplazamiento
    stack.push(inputSymbol);
    stack.push("q" + action.substring(1)); // Agrega 'q' antes del número del estado
    System.out.println("token: "+inputSymbol+" Pila: " + stack +" Accion: "+action);
    
    String PilaTemp ="";
    PilaTemp = stack.toString();
    Object[] datos = new Object[]{inputSymbol, PilaTemp, action};
    modelo.addRow(datos);
    //PrevioSymbol = inputSymbol;
    PrevioSymbol2 = inputSymbol;
} else if (action.startsWith("P")) {
    // Realiza una reducción
    String[] actionParts = action.split("");
    if (actionParts.length >= 2) {
        String production = action;
        List<String> rightHandSide = PRODUCTIONS.get(production);

        // Mensaje de depuración: imprime el contenido de PRODUCTIONS
        //System.out.println("Producción action "+ action);
        //System.out.println("Producción actual: "+ production);
        //System.out.println("Lado derecho de la producción: " + rightHandSide);
        
        if (rightHandSide.contains("€")) {
                    // La producción no puede reducirse a un símbolo vacío
                    isReducing = true;// Establece la bandera para salir del bucle exterior
                } else {
                   
        // Elimina los símbolos de la pila
        for (int i = 0; i < (rightHandSide.size()-1)*2; i++) {
             //System.out.println("Lado derecho de la rightHandSide antes pop: " + rightHandSide.size());
            stack.pop();
           
        }
        }
        
        firstSymbol = rightHandSide.get(0);
    
        String nonTerminal = firstSymbol; //toma el right hand
        String estadoActual = stack.peek();
        
        
        //System.out.println("Pila antes del reduce: " + stack);
        stack.push(nonTerminal);
        // Consulta la tabla de análisis para obtener el próximo estado después de la reducción
        String nextState = getGoto(estadoActual, nonTerminal);
        stack.push(nextState);
        //System.out.println("No Terminal: " + nonTerminal);
        System.out.println("token: "+inputSymbol+" Pila: " + stack +" Accion: "+action);
        String PilaTemp ="";
        PilaTemp = stack.toString();
        Object[] datos = new Object[]{inputSymbol, PilaTemp, action};
        modelo.addRow(datos);
        //System.out.println("Pila después del reduce: " + stack);
        isReducing = true;
        //System.out.println("Nuevo valor del inputSymbol: " + inputSymbol);
        PrevioSymbol= inputSymbol;
        PrevioSymbol2 = inputSymbol;
        
         
    } else {
        System.out.println("Error de sintaxis en el análisis de acción (Reduce): " + action);
        mensajes.setText("Error de sintaxis.");
        return;
    }
} else if (action.equals("Accept")) {
    System.out.println("Análisis sintáctico exitoso.");
    mensajes.setText("Análisis sintáctico exitoso.");
    return;
} else {
    System.out.println("Error de sintaxis (Desconocido): " + action);
    mensajes.setText("Error de sintaxis. "+ "Posibles simbolos aceptables: "+possibleSymbols);
    return;
}
    }
}

private static List<String> Operadores = Arrays.asList("+", "-", "*", "/", "(", ")",";");
private static List<String> TipoaDato = Arrays.asList("int", "float", "char");
// pila de codigo intermedio
private static Stack<String> PilaCodigo = new Stack<>();
private static List<String> OperadoresArit = Arrays.asList("+", "-", "*", "/");
String lexemaAnterior, anteriorTipo,Tipo,resultadoSemantico;
int tipo = 5,contador,V1,V2;
String V1S,V2S,ResultV1V2,Operador;

private void Semantico(String lexema, String token, String Accion) {
    
    System.out.println("Lexema "+lexema+"  token "+token);
    if (Accion == null) {
        Accion = "";
    }
    lexemaAnterior = lexema;
    int MapearTipo[] = {0,1,2};
    String TablaAsignacion[][] = {
                    /* 0  1  2 */
        /*int -   0*/{"1","",""},
        /*float - 1*/{"1","1",""},
        /*char -  2*/{"", "","1"}  
    };
    String TablaOperadores[][] = {
                    /* I  F  C */
        /*int -   0*/{"0","1","2"},
        /*float - 1*/{"1","1",""},
        /*char -  2*/{"" , "",""}  
    };
    if (Operadores.contains(lexema)) {
        // cuando el token es un operador, agregarlo a a la pila de operadores
        System.out.println("Pila Semantica: "+PilaSemantica);
        System.out.println("Pila Operadores: "+PilaOperadores);
        String PilaSem="";
        String PilaOp="";
        PilaSem=PilaSemantica.toString();
        PilaOp = PilaOperadores.toString();
        Object[] datos = new Object[]{PilaSem};
        Object[] datos2 = new Object[]{PilaOp};
        modelo2.addRow(datos);
        modelo3.addRow(datos2);
        if(!PilaOperadores.empty()){
            // ver si el operador que esta en el tope es de igual o mayor prioridad
            // Si el lexema es un punto y coma
            if(lexema.equals(";")){
                // Mientras la pila de operadores no esté vacía
                while(!PilaOperadores.empty()){
                    // Realiza operaciones de desapilado y verifica la compatibilidad de los tipos
                    // de datos para las operaciones
                    V1=PilaSemantica.peek();
                    PilaSemantica.pop();
                    contador -= 1;
                    V1S = PilaCodigo.peek();
                    PilaCodigo.pop();
                    V2=PilaSemantica.peek();
                    V2S = PilaCodigo.peek();
                    PilaSemantica.pop();
                    //contador -=1;
                    PilaCodigo.pop();
                    resultadoSemantico = TablaOperadores[V1][V2];
                    if (resultadoSemantico == "") {
                        System.out.println("Error semantico, el tipo de dato: "+V1+" no puede operar con "+V2);
                        break;
                    }
                    PilaSemantica.push(Integer.parseInt(resultadoSemantico));
                    PilaCodigo.push("V"+contador);
                    ResultV1V2 = PilaCodigo.peek();
                    System.out.println("Codigo intermedio"+PilaCodigo);
                    Operador = PilaOperadores.peek();
                    String PilaCodigoTemp =ResultV1V2 + " = "+ V2S + " " + Operador + " " + V1S;
                    Object[] datos10 = new Object[]{PilaCodigoTemp};
                    modelo4.addRow(datos10);
                    ExpPosfijo.push(PilaOperadores.get(PilaOperadores.size()-1));
                    
                    PilaOperadores.pop();
                    
                }
            }else if(OperadoresArit.contains(lexema) ){
                if(getPriority(lexema) >= getPriority(PilaOperadores.peek())){
                // Si la prioridad del lexema es mayor o igual a la del operador en el tope de la pila
                // Desapila el operador y apila el lexema
                ExpPosfijo.push(PilaOperadores.peek());
                V1 = PilaSemantica.peek();
                V1S = PilaCodigo.peek();
                PilaSemantica.pop();
                contador -= 1;
                PilaCodigo.pop();
                V2=PilaSemantica.peek();
                V2S = PilaCodigo.peek();
                PilaSemantica.pop();
                PilaCodigo.pop();
                resultadoSemantico = TablaOperadores[V1][V2];
                if (resultadoSemantico == "") {
                    System.out.println("Error semantico, el tipo de dato: "+V1+" no puede operar con "+V2);
                }else{
                    PilaSemantica.push(Integer.parseInt(resultadoSemantico));
                    PilaCodigo.push("V"+contador+":"+ lexema);
                    ResultV1V2 = PilaCodigo.peek();
                    System.out.println("Codigo intermedio"+PilaCodigo);
                    Operador = PilaOperadores.peek();
                    String PilaCodigoTemp =ResultV1V2 + " = "+ V2S + " " + Operador + " " + V1S;
                    Object[] datos10 = new Object[]{PilaCodigoTemp};
                    modelo4.addRow(datos10);
                    ExpPosfijo.push(PilaOperadores.get(PilaOperadores.size()-1));
                    PilaOperadores.pop();
                    PilaOperadores.push(lexema);
                }
            }else{
                PilaOperadores.push(lexema);
            }
            }else if(lexema.equals(")")){
                // si es un parentesis derecho, sacar todos los operadores hasta encontrar un parentesis izquierdo
                while(!PilaOperadores.peek().equals("(")){
                    V1=PilaSemantica.peek();
                    V1S = PilaCodigo.peek();
                    PilaSemantica.pop();
                    contador -= 1;
                    PilaCodigo.pop();
                    V2=PilaSemantica.peek();
                    V2S = PilaCodigo.peek();
                    PilaSemantica.pop();
                    //contador -=1;
                    PilaCodigo.pop();
                    resultadoSemantico = TablaOperadores[V1][V2];
                    if (resultadoSemantico == "") {
                        System.out.println("Error semantico, el tipo de dato: "+V1+" no puede operar con "+V2);
                        break;
                    }
                    PilaSemantica.push(Integer.parseInt(resultadoSemantico));
                    PilaCodigo.push("V"+contador);
                    ResultV1V2 = PilaCodigo.peek();
                    System.out.println("Codigo intermedio"+PilaCodigo);
                    Operador = PilaOperadores.peek();
                    String PilaCodigoTemp = ResultV1V2 + " = "+ V2S + " " + Operador + " " + V1S;
                    Object[] datos10 = new Object[]{PilaCodigoTemp};
                    modelo4.addRow(datos10);
                    ExpPosfijo.push(PilaOperadores.get(PilaOperadores.size()-1));
                    PilaOperadores.pop();
                }
                PilaOperadores.pop();
            }
            else{
                // Si ninguna de las condiciones anteriores se cumple, apila el lexema
                PilaOperadores.push(lexema);
            }
        }else{
            // Si la pila de operadores está vacía y el lexema no es un punto y coma, apila el lexema
            if(!lexema.equals(";")){
                PilaOperadores.push(lexema);
            }
       }
       // Cuando la acción es "P8", que podría representar una operación de asignación
        if(Accion.equals("P8")){
            // Obtiene el tipo del valor en la cima de la pila semántica sin eliminarlo
            V1 = PilaSemantica.peek();
            V1S = PilaCodigo.peek();
            // Elimina el valor en la cima de la pila semántica
            PilaSemantica.pop();
            // Decrementa el contador, que podría estar rastreando el número de elementos en la pila
            contador -= 1;
            PilaCodigo.pop();
            // Obtiene el nuevo tipo en la cima de la pila semántica sin eliminarlo
            V2 = PilaSemantica.peek();
            V2S = PilaCodigo.peek();
            PilaCodigo.pop();
            // Busca en la tabla de asignación el resultado semántico para los tipos V1 y V2
            resultadoSemantico = TablaAsignacion[V2][V1];
            // Si el resultado semántico es una cadena vacía, hay un error semántico
            if(resultadoSemantico.equals("")){
                // Imprime un mensaje de error
                System.out.println("Error semantico: no coinciden los tipo de datos en la asignacion");
            }
            PilaCodigo.push("V"+contador);
            ResultV1V2 = PilaCodigo.peek();
            String PilaCodigoTemp = V2S + " " + "=" + " " + V1S;
            Object[] datos10 = new Object[]{PilaCodigoTemp};
            modelo4.addRow(datos10);
            // Imprime un mensaje indicando que la asignación ha finalizado
            System.out.println("Asignacion finalizada");
        }
        System.out.println("Pila Semantica: "+PilaSemantica);
        System.out.println("Pila Operadores: "+PilaOperadores);
        PilaSem="";
         PilaOp="";
        PilaSem=PilaSemantica.toString();
        PilaOp = PilaOperadores.toString();
        Object[] datos3 = new Object[]{PilaSem};
        Object[] datos4 = new Object[]{PilaOp};
        modelo2.addRow(datos3);
        modelo3.addRow(datos4);
    }else {
        // cuando el token es un id, se debe agreagar a la pila semantica
        // Cuando el token es un identificador ("id")
        if(token.equals("id")){
            // Buscar en la tabla de símbolos el lexema anterior
            if(tabladeSimbolos.buscar(lexema).tipo.equals(null)){
                // Si el tipo del lexema es null, no se realiza ninguna acción
            }else{
                // Si el tipo del lexema no es null
                Tipo = tabladeSimbolos.buscar(lexema).tipo; // Asignar el tipo del lexema a la variable Tipo
                for(int i=0; i < TipoaDato.size(); i++){ // Iterar sobre la lista TipoaDato
                    if(Tipo.equals(TipoaDato.get(i))){ // Si el tipo del lexema coincide con un tipo en TipoaDato
                        tipo = MapearTipo[i]; // Asignar el tipo correspondiente de MapearTipo a la variable tipo
                    }
                }
                if(tipo == 5){
                    // Si el tipo es 5, no se realiza ninguna acción
                }else{                    
                    PilaSemantica.push(tipo); // Empujar el tipo a la pila semántica
                    ExpPosfijo.push(lexema); // Empujar el lexema a la pila de expresión posfija
                    contador += 1; // Incrementar el contador
                    PilaCodigo.push("V"+contador+":"+ lexema); // Empujar una nueva variable con el contador y el tipo a la pila de código
                    System.out.println(PilaCodigo); // Imprimir el estado actual de la pila de código
                    String PilaCodigoTemp = PilaCodigo.peek();
                    Object[] datos10 = new Object[]{PilaCodigoTemp};
                    modelo4.addRow(datos10);
                }                
            }
        }
        System.out.println(PilaCodigo);
        if(token.equals("num")){ // Si el token actual es un número
            // buscar el tipo de dato del lexema del token
            if(lexema.contains(".")){ // Si el lexema contiene un punto, es un número flotante
                // float
                PilaSemantica.push(1); // Empuja 1 a la pila semántica para indicar un número flotante
                ExpPosfijo.push(lexema); // Empuja el lexema a la pila de expresión posfija
                contador += 1; // Incrementa el contador
                PilaCodigo.push("V"+contador+":"+ lexema); // Empuja una nueva variable con el contador y el tipo a la pila de código
                System.out.println("Codigo intermedio"+PilaCodigo); // Imprime el código intermedio
                String PilaCodigoTemp = PilaCodigo.peek();
                Object[] datos10 = new Object[]{PilaCodigoTemp};
                modelo4.addRow(datos10);
            }else{ // Si el lexema no contiene un punto, es un número entero
                // integer
                PilaSemantica.push(0); // Empuja 0 a la pila semántica para indicar un número entero
                ExpPosfijo.push(lexema); // Empuja el lexema a la pila de expresión posfija
                contador += 1; // Incrementa el contador
                PilaCodigo.push("V"+contador+":"+lexema); // Empuja una nueva variable con el contador y el tipo a la pila de código
                System.out.println("Codigo intermedio"+PilaCodigo); // Imprime el código intermedio
                String PilaCodigoTemp = PilaCodigo.peek();
                Object[] datos10 = new Object[]{PilaCodigoTemp};
                modelo4.addRow(datos10);
            }
        }
        System.out.println("Pila Semantica: "+PilaSemantica);
        System.out.println("Pila Operadores: "+PilaOperadores);
        
       String PilaSem="";
       String PilaOp="";
        PilaSem=PilaSemantica.toString();
        PilaOp = PilaOperadores.toString();
        Object[] datos3 = new Object[]{PilaSem};
        Object[] datos4 = new Object[]{PilaOp};
        modelo2.addRow(datos3);
        modelo3.addRow(datos4);
    }    
    System.out.println(ExpPosfijo);
}


public static int getPriority(String operator) {
    switch (operator) {
        case "+":
        case "-":
            return 2;
        case "*":
        case "/":
            return 1;
        default:
            return 3;
    }
}


private String getAction(String state, String token) {
    int column = Arrays.asList(encabezadosColumnas).indexOf(token.toLowerCase());
    int row = Arrays.asList(encabezadosRenglones).indexOf(state.toLowerCase());

    // Resto del código permanece igual
    //System.out.println("Token: " + token + ", Column Index: " + column);
    //System.out.println("State: " + state + ", Row Index: " + row);

    if (row >= 0 && column >= 0 && matriz[row][column].isEmpty()) {
        System.out.println("Celda vacía en la matriz. Comparando con todos los encabezados de columnas.");
        possibleSymbols = "";
        for (String symbol : encabezadosColumnas) {
            int possibleColumn = Arrays.asList(encabezadosColumnas).indexOf(symbol.toLowerCase());
            if (possibleColumn >= 0 && possibleColumn < matriz[0].length) {
                String possibleAction = matriz[row][possibleColumn];
                if (!possibleAction.isEmpty()) {
                    possibleSymbols += symbol + ", ";
                }
            }
        }

        if (!possibleSymbols.isEmpty()) {
            System.out.println("Símbolos aceptables: " + possibleSymbols);
        } else {
            System.out.println("No hay símbolos aceptables para este estado.");
        }

        return "Invalid";
    } else if (row >= 0 && column >= 0) {
        String action = matriz[row][column];
        //System.out.println("Value in Matrix: " + action);
        return action;
    } else {
        System.out.println("Invalid indices. Row: " + row + ", Column: " + column);
        System.out.println("No hay símbolos aceptables para este estado.");
        return "Invalid";
    }
}




  private String getGoto(String state, String nonTerminal) {
        int column = Arrays.asList(encabezadosColumnas).indexOf(nonTerminal);
        int row = Arrays.asList(encabezadosRenglones).indexOf(state);
       if (row >= 0 && column >= 0) {
        //System.out.println("Value in Matrix: " + matriz[row][column]);
        return matriz[row][column];
    } else {
        System.out.println("Invalid indices. Row: " + row + ", Column: " + column);
        return "Invalid";
    }
    }





    
    
    
   
    
    
    
    private boolean tipo(String token) {
        List<String> tipos = Arrays.asList("int", "float", "char");
        boolean resultado = tipos.contains(token);
        return resultado;
    }

    
    private boolean terminal(String Ptoken,String token){
        for (String encabezadosColumna : encabezadosColumnas) {
            if(encabezadosColumna.equals(Ptoken)){
                return !Ptoken.equals(token);
            }
        }
        return false;
    }
    private void llenarJPnaleTokens() {

        tokens.forEach(token -> {
            lexemas.add(token.getLexeme());
            variable = token.getLine();

            //solucion rapida para imprimir el salto de linea casda que se encuentre cambio en la linea 
            if (variable != valorAnterior) {
                lexico.setText(lexico.getText() + "\n");// Imprimir salto de línea
                valorAnterior = token.getLine();
            }
            if (token.getLexicalComp() == "pReservada" || token.getLexicalComp() == "tipoDa") {

                lexico.setText(lexico.getText() + " " + token.getLexeme());
                //Imprimir el token en mi JTextpanel de mi analizador lexico 
            } else if (token.getLexicalComp() == "ERROR") {
                lexico.setText(lexico.getText() + " ");
            } else {

                lexico.setText(lexico.getText() + " " + token.getLexicalComp());
            }

        });
    }

    //Metodo que busca un elemento dentro del encabezadosRenglones y devuelve el indice o posicion donde lo encontro
    public int Renglon() {
        for (int i = 0; i < encabezadosRenglones.length; i++) {
            if (encabezadosRenglones[i].equals(pila.peek())) {
                renglones = i;
                break;
            }
        }
        return renglones;
    }

   

    //Metodo que busca la posicion del token dentro de encabezadosColumnas y devuelve su posicion
    public int Columna(String token) {
        for (int i = 0; i < encabezadosColumnas.length; i++) {
            if (token.equals(encabezadosColumnas[i])) {
                columnas = i;
                break;
            }
        }
        return columnas;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        mensajes = new javax.swing.JTextPane();
        jScrollPane7 = new javax.swing.JScrollPane();
        PilaSin = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        PilaSem = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        PilaOp = new javax.swing.JTable();
        jScrollPane10 = new javax.swing.JScrollPane();
        CodigoInt = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        escritura = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        lexico = new javax.swing.JTextPane();
        bRun = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bAbrir = new javax.swing.JButton();
        bGuardar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane3.setViewportView(mensajes);

        jTabbedPane1.addTab("Sintactico y Errores", jScrollPane3);

        PilaSin.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Token", "Pila", "Accion"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane7.setViewportView(PilaSin);

        jTabbedPane1.addTab("Pila Sintactica", jScrollPane7);

        PilaSem.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Pila Semantica"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(PilaSem);

        jTabbedPane1.addTab("Pila Semantica", jScrollPane4);

        PilaOp.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Pila Operadores"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(PilaOp);

        jTabbedPane1.addTab("Pila Operadores", jScrollPane6);

        CodigoInt.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Codigo Intermedio"
            }
        ));
        jScrollPane10.setViewportView(CodigoInt);

        jTabbedPane1.addTab("Codigo Intermedio", jScrollPane10);

        jScrollPane1.setViewportView(escritura);

        jScrollPane2.setViewportView(lexico);

        bRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/boton-de-play.png"))); // NOI18N
        bRun.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        bRun.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/jugar (1).png"))); // NOI18N
        bRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRunActionPerformed(evt);
            }
        });

        bNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/agregar-archivo.png"))); // NOI18N
        bNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        bNuevo.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/agregar-archivo (1).png"))); // NOI18N
        bNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bNuevoActionPerformed(evt);
            }
        });

        bAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/caja (1).png"))); // NOI18N
        bAbrir.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        bAbrir.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/caja (2).png"))); // NOI18N
        bAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAbrirActionPerformed(evt);
            }
        });

        bGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/disquete (1).png"))); // NOI18N
        bGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        bGuardar.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/salvar (1).png"))); // NOI18N
        bGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bGuardarActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("CODIGO");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("LEXICO");

        jMenu1.setText("Archivo");

        jMenuItem1.setText("Nuevo");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Abrir");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("Guardar");
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Run");

        jMenuItem4.setText("Compilar");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Salir");
        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 705, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(bNuevo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bAbrir)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bGuardar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bRun))
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bAbrir)
                    .addComponent(bNuevo)
                    .addComponent(bGuardar)
                    .addComponent(bRun))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addGap(32, 32, 32)))
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void bRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRunActionPerformed
        StyledDocument doc = escritura.getStyledDocument();
          
        pila.clear();
        pilaaux.clear();
        banP = true;
        banpr = true;
        renglones = 0;
        columnas = 0;
        mensajes.setText("");
        
        
        if (escritura.getText().isEmpty()){
         mensajes.setText("No hay nada que analizar");
        
        }else {
          borrarComponentes();
        analisisLexico();
        llenarJPnaleTokens();
        analisisLexicoEerr();
         List<String> input =(tokens.stream().map(Token::toString).collect(Collectors.toList()));

        PrevioSymbol2 = "";
        resetParserState();
        parse();
        
        //noDuplicados();
        
        }
 
    }//GEN-LAST:event_bRunActionPerformed

    private void bNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNuevoActionPerformed
        directorio.New();
        borrarComponentes();// TODO add your handling code here:
    }//GEN-LAST:event_bNuevoActionPerformed

    private void bAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAbrirActionPerformed
        if (directorio.Open()) {

            borrarComponentes();
        }
    }//GEN-LAST:event_bAbrirActionPerformed

    private void bGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bGuardarActionPerformed
        if (directorio.Save()) {
            borrarComponentes();
        }
    }//GEN-LAST:event_bGuardarActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        directorio.New();
        borrarComponentes();// TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if (directorio.Open()) {

            borrarComponentes();
        }   // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        pila.clear();
        pilaaux.clear();
        banP = true;
        banpr = true;
        renglones = 0;
        columnas = 0;

        borrarComponentes();
        analisisLexico();
        llenarJPnaleTokens();
        analisisLexicoEerr();
       
        //imprimirConsola();        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    public void cosasVisuales() {

    }

    
    public void noDuplicados(){
    
    String[] lines = mensajes.getText().split("\n");
                Set<String> set = new LinkedHashSet<String>(Arrays.asList(lines));

                String noDuplicateText = String.join("\n", set);
                mensajes.setText(noDuplicateText);
    
    }
    
    /// AQUI ES PARA EL CODIGO DE FERNANDO
    
    
    
    private void borrarComponentes() {

        lexico.setText("");
        valorAnterior = 1;
        variable = 1;
        mensajes.setText("");
        tokens.clear();
        // errors.clear();
        // identProd.clear();
        // identificadores.clear();
    }
    
    
     // ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // COLOREAR LAS COSAS
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //METODO PARA ENCONTRAR LAS ULTIMAS CADENAS
    private int findLastNonWordChar(String text, int index) {
        while (--index >= 0) {
            //  \\W = [A-Za-Z0-9]
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
        }
        return index;
    }

    //METODO PARA ENCONTRAR LAS PRIMERAS CADENAS 
    private int findFirstNonWordChar(String text, int index) {
        while (index < text.length()) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
            index++;
        }
        return index;
    }   
 //METODO PARA PINTAS LAS PALABRAS RESEVADAS
    private void colors() {

        final StyleContext cont = StyleContext.getDefaultStyleContext();

        //COLORES 
        final AttributeSet attred = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(225, 123, 13));//naranja
        final AttributeSet attgreen = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(48, 101, 59)); //verde
        final AttributeSet attblue = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(0, 0, 0)); //Blanco
        final AttributeSet attblack = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(145, 55, 76));
        final AttributeSet blanco = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(0, 0, 0)); //Blanco
        //STYLO 
        DefaultStyledDocument doc = new DefaultStyledDocument() {
            public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offset);
                if (before < 0) {
                    before = 0;
                }
                int after = findFirstNonWordChar(text, offset + str.length());
                int wordL = before;
                int wordR = before;

                while (wordR <= after) {
                    if (wordR == after || String.valueOf(text.charAt(wordR)).matches("\\W")) {
                        if (text.substring(wordL, wordR).matches("(\\W)*()")) { //Azul 
                            setCharacterAttributes(wordL, wordR - wordL, attblue, false);
                        } else if (text.substring(wordL, wordR).matches("(\\W)*(para|verdadero|falso|mensaje|lectura)")) { //verde
                            setCharacterAttributes(wordL, wordR - wordL, attgreen, false);
                        } else if (text.substring(wordL, wordR).matches("(\\W)*(Inicio|metodo|si|sino|hacer|mientras)")) { // blanco
                            setCharacterAttributes(wordL, wordR - wordL, attred, false);
                        } 
                         else if (text.substring(wordL, wordR).matches("(\\W)*($)")) { // blanco
                            setCharacterAttributes(wordL, wordR - wordL, blanco, false);
                        
                         }else {
                            setCharacterAttributes(wordL, wordR - wordL, attblack, false);
                        }
                        wordL = wordR;

                    }
                    wordR++;
                }
            }

            public void romeve(int offs, int len) throws BadLocationException {
                super.remove(offs, len);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offs);
                if (before < 0) {
                    before = 0;
                }
            }
        };

        JTextPane txt = new JTextPane(doc);
        String temp = escritura.getText();
        escritura.setStyledDocument(txt.getStyledDocument());
        escritura.setText(temp);

    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    
    

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Com().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable CodigoInt;
    private javax.swing.JTable PilaOp;
    private javax.swing.JTable PilaSem;
    private javax.swing.JTable PilaSin;
    private javax.swing.JButton bAbrir;
    private javax.swing.JButton bGuardar;
    private javax.swing.JButton bNuevo;
    private javax.swing.JButton bRun;
    private javax.swing.JTextPane escritura;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextPane lexico;
    private javax.swing.JTextPane mensajes;
    // End of variables declaration//GEN-END:variables
}
