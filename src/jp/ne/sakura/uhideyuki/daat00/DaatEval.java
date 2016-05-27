/** BRT -- Bunny RunTime (or "Be Right There).
 */
package jp.ne.sakura.uhideyuki.daat00;

import java.util.Stack;
import java.util.Arrays;

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
    public Atom[] args;
    public CallCont(Atom[] as){ args = as; }
}

class DaatEvalApply {
    private Stack<Cont> s;
    private Expr code;

    public DaatEvalApply(){
	s = new Stack<Cont>();
    }

    public Expr eval(Expr e){
	code = e;
	while (runStep()){}
	return e;
    }

    public Boolean runStep(){
	if (code instanceof LetExpr){
	    evalLet();
	} else if (code instanceof CaseExpr){
	    evalCase(); // CASECON, CASEANY or CASE
	} else if (code.isLitOrValue() && !s.empty() && (s.peek() instanceof CaseCont)){
	    evalRet();
	} else if (code.isThunk()){
	    evalThunk();
	} else if (code.isValue() && !s.empty() && (s.peek() instanceof UpdCont)){
	    evalUpdate();
	} else if (code.isKnownCall()){
	    evalKnownCall();
	} else if (code instanceof PrimOpExpr){
	    evalPrimOp();
	} else if (code instanceof FunAppExpr){
	    Expr f = ((FunAppExpr)code).f;
	    if (f.isFunObj()){
		evalFun(); // EXACT, CALLK or PAP2
	    } else if (f.isThunk()){	
		evalTCall();
	    } else {
		assert f.isPapObj();
		evalPCall();
	    }
	} else if (!s.empty() && s.peek() instanceof CallCont){
	    evalRetFun();
	} else {
	    assert (code.isValue() || code.isLiteral()) && s.empty();
	    return false;
	}
	return true;
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

    private void evalPrimOp(){
	PrimOpExpr e = (PrimOpExpr) code;
	assert e.lambda.arity() == e.args.length;
	code = e.lambda.call(e.args);
    }

    private void evalFun(){
	FunAppExpr e = (FunAppExpr) code;
	FunObj fobj = (FunObj) e.f.getObj();

	if (e.args.length == fobj.arity){ // EXACT
	    code = fobj.lambda.call(e.args);
	} else if (e.args.length > fobj.arity){ // CALLK
	    Atom[] args1 = Arrays.copyOfRange(e.args, 0, fobj.arity);
	    code = fobj.lambda.call(args1);

	    Atom[] args2 = 
		Arrays.copyOfRange(e.args, fobj.arity, e.args.length);
	    s.push(new CallCont(args2));
	} else { // PAP2
	    PapObj pap = new PapObj(e.f, e.args);
	    code = new AtomExpr(new Var(pap));
	}
    }

    private void evalTCall(){
	FunAppExpr e = (FunAppExpr) code;
	code = e.f;
	s.push(new CallCont(e.args));
    }

    private Atom[] catArgs(Atom[] a1, Atom[] a2){
	Atom[] r = new Atom[a1.length + a2.length];
	System.arraycopy(a1, 0, r, 0, a1.length);
	System.arraycopy(a2, 0, r, a1.length, a2.length);
	return r;
    }

    private void evalPCall(){
	FunAppExpr e = (FunAppExpr) code;
	PapObj pap = (PapObj) e.f.getObj();
	Expr g = pap.f;
	Atom[] args2 = catArgs(pap.args, e.args);
	code = new FunAppExpr(g, args2, -1);
    }

    private void evalRetFun(){
	Expr f = code;
	assert f.isFunObj() || f.isPapObj();
	CallCont c = (CallCont) s.pop();
	code = new FunAppExpr(f, c.args, -1);
    }
}
