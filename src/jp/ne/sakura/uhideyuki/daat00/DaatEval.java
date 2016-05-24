package jp.ne.sakura.uhideyuki.daat00;

import java.util.Stack;

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
	if        (code instanceof LetExpr){
	    evalLet();
	} else if (code instanceof CaseExpr) {
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

    private Boolean CaseCon(){
	return false; // dummy
    }

    private Boolean CaseAny(){
	return false; // dummy
    }
}
