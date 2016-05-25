package jp.ne.sakura.uhideyuki.daat00;

import java.util.Stack;
import java.lang.Error;

abstract class Cont {}

class CaseCont extends Cont {
    public Alt[] alts;
    public CaseCont(Alt[] as){ alts = as; }
}

class UpdCont extends Cont {
    public Expr x;
    public UpdCont(Expr t){ assert t.isVar(); x = t; }
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
	} else if (code.isLitOrValue() && (s.peek() instanceof CaseCont)){
	    evalRet();
	} else if (code.isThunk()){
	    evalThunk();
	} else if (code.isValue() && (s.peek() instanceof UpdCont)){
	    evalUpdate();
	} else if (code.isKnownCall()){
	    evalKnownCall();
	}
	return false;
    }

    private void evalLet(){
	LetExpr e = (LetExpr) code;
	Var[] args = new Var[e.es.length];

	for (int i = 0; i < e.es.length; i++){
	    Thunk t = new Thunk(e.es[i]);
	    args[i] = new Var(t);
	}

	code = e.lambda.call(args);
    }

    private void evalCase(){
	if (CaseCon() || CaseAny()){
	    return;
	}
	CaseExpr e = (CaseExpr) code;
	CaseCont c = new CaseCont(e.alts);
	s.push(c);
	code = e.scrut;
    }

    private Boolean CaseCon(){
	CaseExpr e = (CaseExpr) code;
	Expr scrut = e.scrut;
	Alt[] alts = e.alts;

	if (scrut.isConObj()){
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

    private DefaultAlt getDefaultAlt(Alt[] alts){
	for (int i = 0; i < alts.length; i++){
	    Alt alt = alts[i];
	    if (alt instanceof DefaultAlt){
		return (DefaultAlt) alt;
	    }
	}
	assert true : "Default Alt not found.";
	return null;
    }
    
    private Boolean CaseAny(){
	CaseExpr e = (CaseExpr) code;
	Expr scrut = e.scrut;
	Alt[] alts = e.alts;

	if (scrut.isLitOrValue()){
	    DefaultAlt dalt = getDefaultAlt(alts);
	    Atom[] a = new Atom[1];
	    a[0] = ((AtomExpr) scrut).a;
	    code = dalt.lambda.call(a);
	    return true;
	}
	return false;
    }

    private void evalRet(){
	CaseCont cont = (CaseCont) s.pop();
	CaseExpr e = new CaseExpr(code, cont.alts);
	code = e;
    }

    private void evalThunk(){
	UpdCont upd = new UpdCont(code);
	Thunk t = (Thunk) ((Var)((AtomExpr)code).a).obj;
	code = t.e;
	s.push(upd);
    }

    private void evalUpdate(){
	UpdCont upd = (UpdCont) s.pop();
	Expr x = upd.x;
	assert x.isThunk();
	Var v = (Var)((AtomExpr)x).a;
	v.obj = ((Var)((AtomExpr)code).a).obj;
	assert x.isValue();
    }

    private void evalKnownCall(){
	FunAppExpr e = (FunAppExpr) code;
	assert e.arity == e.args.length;

	Expr f = e.f;
	assert f.isValue();
	HeapObj obj = ((Var)((AtomExpr)f).a).obj;

	assert obj instanceof FunObj;
	FunObj fobj = (FunObj) obj;
	assert fobj.arity == e.arity;

	code = fobj.lambda.call(e.args);
    }
}
