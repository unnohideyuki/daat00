package jp.ne.sakura.uhideyuki.daat00;

import java.util.Stack;
import java.lang.Error;

abstract class Cont {}

class CaseCont extends Cont {
    public Alt[] alts;
}

class UpdCont extends Cont {
    public Var x;
}

class CallCont extends Cont {
    public Var[] args;
}

class DaatEvalApply {
    private Stack<Cont> s;
    private Expr code;

    public DaatEvalApply(){
	s = new Stack<Cont>();
    }

    public void eval(Expr e){
	code = e;
	while (runStep()){}
    }

    public Boolean runStep(){
	if (code instanceof LetExpr){
	    evalLet();
	} else if (code instanceof CaseExpr){
	    evalCase();
	}

	return false;
    }

    private void evalLet(){
	LetExpr e = (LetExpr) code;
	Var[] args = new Var[e.es.length];

	for (int i = 0; i < e.es.length; i++){
	    Thunk t = new Thunk();
	    t.e = e.es[i];
	    args[i] = new Var();
	    args[i].obj = t;
	}

	code = e.lambda.call(args);
    }

    private void evalCase(){
	if (CaseCon() || CaseAny()){
	    return;
	}
	CaseExpr e = (CaseExpr) code;
	CaseCont c = new CaseCont();
	c.alts = e.alts;
	s.push(c);
	code = e.scrut;
    }

    private Boolean isConObj(Expr e){
	Boolean r = ((e instanceof AtomExpr) &&
		     (((AtomExpr)e).a instanceof Var) &&
		     (((Var)((AtomExpr)e).a).obj instanceof ConObj));
	return r;
    }

    private Boolean CaseCon(){
	CaseExpr e = (CaseExpr) code;
	Expr scrut = e.scrut;
	Alt[] alts = e.alts;

	if (isConObj(scrut)){
	    ConObj cobj = (ConObj) ((Var)((AtomExpr)scrut).a).obj;
	    Cotr cotr = cobj.cotr;

	    CotrAlt calt = null;
	    for (int i = 0; i < alts.length; i++){
		Alt alt = alts[i];
		if (alt instanceof CotrAlt){
		    CotrAlt t = (CotrAlt) alt;
		    if (cotr.equals(t.cotr)){
			calt = t;
			break;
		    }
		}
	    }

	    if (calt != null){
		Atom[] args = cobj.args;
		code = calt.lambda.call(args);
		return true;
	    }
	}

	return false;
    }

    private Boolean isLiteral(Expr e){
	Boolean r = ((e instanceof AtomExpr) &&
		     (((AtomExpr)e).a instanceof Literal));
	return r;
    }

    private Boolean isValue(Expr e){
	if ((e instanceof AtomExpr) && (((AtomExpr)e).a instanceof Var)){
	    Var v = (Var) ((AtomExpr)e).a;
	    Boolean r = ((v.obj instanceof FunObj) ||
			 (v.obj instanceof PapObj) ||
			 (v.obj instanceof ConObj));
	    return r;
	}
	return false;
    }

    private Boolean isLitOrValue(Expr e){
	Boolean r = isLiteral(e) || isValue(e);
	return r;
    }

    private DefaultAlt getDefaultAlt(Alt[] alts){
	for (int i = 0; i < alts.length; i++){
	    Alt alt = alts[i];
	    if (alt instanceof DefaultAlt){
		return (DefaultAlt) alt;
	    }
	}
	return null;
    }
    
    private Boolean CaseAny(){
	CaseExpr e = (CaseExpr) code;
	Expr scrut = e.scrut;
	Alt[] alts = e.alts;

	if (isLitOrValue(scrut)){
	    DefaultAlt dalt = getDefaultAlt(alts);
	    Atom[] a = new Atom[1];
	    a[0] = ((AtomExpr) scrut).a;
	    code = dalt.lambda.call(a);
	    return true;
	}
	return false;
    }
}
