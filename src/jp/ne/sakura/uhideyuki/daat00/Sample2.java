package jp.ne.sakura.uhideyuki.daat00;

/* javac *.java
 * java -ea -cp ${HOME}/prj/daat00/src/ jp.ne.sakura.uhideyuki.daat00.Sample1
 */

/* Sample1

It is a sample of STG evaludation, corresponding to:

  main = let
    s = "Hello, Let Expression!"
   in putStrLn s

 */
public class Sample2 {
    public static void main(String[] args){
	Expr[] es = {RT.fromString("Hello, Let Expression!")};
	Expr e = new LetExpr(es, Codes.body);
	RT.eval(e);
    }
}

class Codes {
    public static MainBody body = new MainBody();
}

class MainBody implements LambdaForm {
    public int arity(){ return 1; }
    public Expr call(Atom[] args){
	Expr s = new AtomExpr(args[0]);
	return RT.app(RT.putStrLn, s);
    }
}
