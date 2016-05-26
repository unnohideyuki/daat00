package jp.ne.sakura.uhideyuki.daat00;

/* javac *.java
 * java -ea -cp ${HOME}/prj/daat00/src/ jp.ne.sakura.uhideyuki.daat00.Sample1
 */

public class Sample1 {
    private static Expr mkExpr(HeapObj obj){
	return new AtomExpr(new Var(obj));
    }

    private static Expr mkExpr(char c){
	return new AtomExpr(new LitChar(c));
    }

    private static Expr mkFun(LambdaForm lam){
	return mkExpr(new FunObj(lam.arity(), lam));
    }

    private static Expr app(Expr f, Expr a1, Expr a2){
	Expr[] args = {a1, a2};
	return mkExpr(new Thunk(new FunAppExpr(f, args, -1)));
    }

    private static Expr app1(Expr f, Expr a){
	Expr[] args = {a};
	return mkExpr(new Thunk(new FunAppExpr(f, args, -1)));
    }

    public static void main(String[] args){
	Expr cH = mkExpr('H');
	Expr ci = mkExpr('i');
	Expr cons = mkFun(new ConsFunc());
	Expr nill = mkExpr(new ConObj(new Cotr("Prim.[]"), new Atom[0]));

	Expr hi = app(cons, cH, app(cons, ci, nill));
	Expr putstrln = mkFun(new PutStrLnFunc());
	Expr e = app1(putstrln, hi);

	DaatEvalApply rt = new DaatEvalApply();
	rt.eval(e);
    }


}

class ConsFunc implements LambdaForm {
    public int arity(){ return 2; }
    public Expr call(Atom[] args){
	assert args.length == 2;
	return new AtomExpr(new Var(new ConObj(new Cotr("Prim.:"), args)));
    }
}

class PutStrLnFunc implements LambdaForm {
    private Expr mkExpr(HeapObj obj){
	return new AtomExpr(new Var(obj));
    }
    
    private Expr unit(){ 
	return mkExpr(new ConObj(new Cotr("Prim.()"), new Atom[0]));
    }

    public int arity(){ return 1; }

    public Expr call(Atom[] args){
	assert args.length == 1;
	/* to be written, print loop here. */
	System.out.print(String.valueOf('\n'));
	return unit();
    }
}

