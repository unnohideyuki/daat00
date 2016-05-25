package jp.ne.sakura.uhideyuki.daat00;

class Cotr {
    public String ident;
    public Boolean equald(Cotr c){ return ident.equals(c.ident); }
}

abstract class Atom {}

class Var extends Atom { 
    public HeapObj obj; 
    public Var(HeapObj x){ obj = x; }
}

abstract class Literal extends Atom {}

class LitInt extends Literal { public int value; }

class LitFloat extends Literal { public Double value; }

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

    public Boolean isThunk(){
	Boolean r = this.isVar() && 
	    ((Var)((AtomExpr)this).a).obj instanceof Thunk;
	return r;
    }

    public Boolean isConObj(){
	Boolean r = ((this instanceof AtomExpr) &&
		     (((AtomExpr)this).a instanceof Var) &&
		     (((Var)((AtomExpr)this).a).obj instanceof ConObj));
	return r;
    }

    public Boolean isKnownCall(){
	Boolean r = ((this instanceof FunAppExpr) &&
		     (((FunAppExpr)this).arity > 0));
	return r;
    }
}

class AtomExpr extends Expr { public Atom a; }

class FunAppExpr extends Expr { 
    public Expr f;
    public Atom[] args;
    public int arity;
}

class PrimOpExpr extends Expr {
    public String primId;
    public Atom[] args;
}

interface LambdaForm {
    public int arity = -1;
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
    public int arity = -1;
    public LambdaForm lambda;
}

class PapObj extends HeapObj {
    public Var f;
    public Atom[] args;
}

class ConObj extends HeapObj {
    public Cotr cotr;
    public Atom[] args;
}

class Thunk extends HeapObj {
    public Expr e;
    public Thunk(Expr x){ e = x; }
}

class BlackHole extends HeapObj {}



