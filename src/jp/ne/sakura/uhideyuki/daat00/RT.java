package jp.ne.sakura.uhideyuki.daat00;

class ConsFunc implements LambdaForm {
    public int arity(){ return 2; }
    public Expr call(Atom[] args){
	assert args.length == 2;
	return new AtomExpr(new Var(new ConObj(new Cotr("Prim.:"), args)));
    }
}

class PutStrLnFunc implements LambdaForm {
    public int arity(){ return 1; }

    private Expr mkExpr(HeapObj obj){
	return new AtomExpr(new Var(obj));
    }

    private Expr unit(){ 
	return mkExpr(new ConObj(new Cotr("Prim.()"), new Atom[0]));
    }

    public Expr call(Atom[] args){
	assert args.length == 1;
	String t = RT.toString(RT.eval(new AtomExpr(args[0])));
	System.out.print(t + "\n");
	return unit();
    }
}

class RT {
    private static Expr mkExpr(HeapObj obj){
	return new AtomExpr(new Var(obj));
    }

    private static Expr mkExpr(char c){
	return new AtomExpr(new LitChar(c));
    }

    private static Expr mkFun(LambdaForm lam){
	return mkExpr(new FunObj(lam.arity(), lam));
    }

    static DaatEvalApply rt = new DaatEvalApply();
    static Expr eval(Expr e){
	return rt.eval(e);
    }

    static Expr putStrLn = mkFun(new PutStrLnFunc());
    static Expr cons = mkFun(new ConsFunc());

    public static String toString(Expr s){
	String t = "";
	
	while(true){
	    if (s.isConObj()){
		ConObj con = (ConObj)((Var)((AtomExpr)s).a).obj;
		if(con.cotr.ident == "Prim.:"){
		    Expr c = RT.eval(new AtomExpr(con.args[0]));
		    t = t + ((LitChar)((AtomExpr)c).a).value;
		    s = RT.eval(new AtomExpr(con.args[1]));
		} else {
		    break;
		}
	    }
	}

	return t;
    }

    public static Expr app(Expr f, Expr[] args){
	return mkExpr(new Thunk(new FunAppExpr(f, args, -1)));
    }

    public static Expr app(Expr f, Expr a){
	Expr[] args = {a};
	return mkExpr(new Thunk(new FunAppExpr(f, args, -1)));
    }

    public static Expr app(Expr f, Expr a1, Expr a2){
	Expr[] args = {a1, a2};
	return mkExpr(new Thunk(new FunAppExpr(f, args, -1)));
    }

    public static Expr fromString(char[] s){
	Expr nil = mkExpr(new ConObj(new Cotr("Prim.[]"), new Atom[0]));

	if (s.length == 0){
	    return nil;
	} else if (s.length == 1){
	    Expr[] args = {mkExpr(s[0]), nil};
	    return app(cons, args);
	}

	char[] t = new char[s.length - 1];
	System.arraycopy(s, 1, t, 0, s.length - 1);

	Expr[] args = { mkExpr(s[0]), fromString(t) };
	return app(cons, args);
    }

    public static Expr fromString(String s){
	return fromString(s.toCharArray());
    }
}
