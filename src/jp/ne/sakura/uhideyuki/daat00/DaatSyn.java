package jp.ne.sakura.uhideyuki.daat00;

class Cotr {
    public String ident;
    public Boolean equald(Cotr c){ return ident.equals(c.ident); }
    public Cotr(String s){ ident = s; }
}

abstract class Atom {}

class Var extends Atom { 
    public HeapObj obj; 
    public Var(HeapObj x){ obj = x; }
}

abstract class Literal extends Atom {}

class LitInt extends Literal { public int value; }

class LitFloat extends Literal { public Double value; }

class LitChar extends Literal { 
    public char value; 
    public LitChar(char c){ value=c; }
}

abstract class Expr {
    public Boolean isVar(){
	Boolean r = (this instanceof AtomExpr) && 
	    (((AtomExpr)this).a instanceof Var);
	return r;
    }

    public Boolean isLiteral(){
	Boolean r = ((this instanceof AtomExpr) &&
		     (((AtomExpr)this).a instanceof Literal));
	return r;
    }

    public Boolean isValue(){
	if (this.isVar()){
	    HeapObj obj = ((Var)((AtomExpr)this).a).obj;
	    Boolean r = ((obj instanceof FunObj) ||
			 (obj instanceof PapObj) ||
			 (obj instanceof ConObj));
	    return r;
	}
	return false;
    }

    public Boolean isLitOrValue(){
	return this.isLiteral() || this.isValue();
    }

    public HeapObj getObj(){
	assert this.isVar();
	return ((Var)((AtomExpr)this).a).obj;
    }

    public Boolean isThunk(){
	return isVar() && getObj() instanceof Thunk;
    }

    public Boolean isConObj(){
	return isVar() && getObj() instanceof ConObj;
    }

    public Boolean isFunObj(){
	return isVar() && getObj() instanceof FunObj;
    }

    public Boolean isPapObj(){
	return isVar() && getObj() instanceof PapObj;
    }

    public Boolean isKnownCall(){
	Boolean r = ((this instanceof FunAppExpr) &&
		     (((FunAppExpr)this).arity > 0));
	return r;
    }
}

class AtomExpr extends Expr { 
    public Atom a; 
    public AtomExpr (Atom b){ a = b; }
}

class FunAppExpr extends Expr { 
    public Expr f;
    public Atom[] args;
    public int arity;
    public FunAppExpr(Expr g, Atom[] as, int n){ f=g; args=as; arity=n; }
    public FunAppExpr(Expr g, Expr[] as, int n){
	f=g;
	args = new Atom[as.length];
	for (int i = 0; i < as.length; i++){
	    assert as[i] instanceof AtomExpr;
	    args[i] = ((AtomExpr)as[i]).a;
	}
	arity=n;
    }
}

class PrimOpExpr extends Expr {
    public LambdaForm lambda;
    public Atom[] args;
}

interface LambdaForm {
    public int arity();
    public Expr call(Atom[] args);
}

class LetExpr extends Expr {
    public Expr[] es;
    public LambdaForm lambda;
}

abstract class Alt {}

class CotrAlt extends Alt {
    public Cotr cotr;
    public LambdaForm lambda;
}

class DefaultAlt extends Alt {
    public LambdaForm lambda;
}

class CaseExpr extends Expr {
    public Expr scrut;
    public Alt[] alts;
    public CaseExpr(Expr e, Alt[] as){ scrut=e; alts=as; }
}

abstract class HeapObj {}

class FunObj extends HeapObj {
    public int arity;
    public LambdaForm lambda;
    public FunObj(int a, LambdaForm lam){ arity=a; lambda=lam; }
}

class PapObj extends HeapObj {
    public Expr f;
    public Atom[] args;
    public PapObj(Expr g, Atom[] as){ f = g; args = as; }
}

class ConObj extends HeapObj {
    public Cotr cotr;
    public Atom[] args;
    public ConObj(Cotr c, Atom[] as){ cotr = c; args = as; }
}

class Thunk extends HeapObj {
    public Expr e;
    public Thunk(Expr x){ e = x; }
}

class BlackHole extends HeapObj {}



