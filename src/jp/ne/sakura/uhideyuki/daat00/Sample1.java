package jp.ne.sakura.uhideyuki.daat00;

/* javac *.java
 * java -ea -cp ${HOME}/prj/daat00/src/ jp.ne.sakura.uhideyuki.daat00.Sample1
 */

/* Sample1

It is a sample of STG evaludation, corresponding to:

  main = putStrLn "Hello, BRT!"

 */
public class Sample1 {
    public static void main(String[] args){
	Expr[] as = {RT.fromString("Hello, BRT!")};
	Expr e = RT.app(RT.putStrLn, as);
	RT.eval(e);

    }
}

