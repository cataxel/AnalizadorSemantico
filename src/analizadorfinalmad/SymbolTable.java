package analizadorfinalmad;

import java.util.HashMap;
import java.util.Iterator;

class Simbolo{
    String nombre,valor,tipo;
    public Simbolo(String nombre, String valor, String tipo){
    this.nombre = nombre;
    this.valor = valor;
    this.tipo = tipo;
    }
}

public class SymbolTable {
    HashMap<String, Simbolo> t;
    public SymbolTable() {
        t = new HashMap<String, Simbolo>();
    }
    public Simbolo insertar(String nombre, String valor, String tipo){
        /*if (t.containsKey(nombre)){
            throw new RuntimeException("Variable ya declarada: " + nombre);
        }*/
        Simbolo s = new Simbolo(nombre, valor,tipo);
        t.put(nombre, s);
        return s;
    }
    public Simbolo buscar(String nombre){
        return (Simbolo) t.get(nombre);
    }
    public void imprimir(){
        Iterator it = t.values().iterator();
        while(it.hasNext()){
            Simbolo s = (Simbolo) it.next();
            System.out.println(s.nombre + ": " + s.valor+ " : "+ s.tipo);
        }
    }
}
