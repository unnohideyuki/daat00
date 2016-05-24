package jp.ne.sakura.uhideyuki.daat00;

class Cotr { public String ident; }

abstract class Atom {}

class Var extends Atom { public HeapObj obj; }

abstract class Literal extends Atom {}

class LitInt extends Literal { public int value; }

class LitFloat extends Literal { public Double value; }

abstract class Expr {}

class AtomExpr extends Expr { public Atom a; }

class FunAppExpr extends Expr { 
    public Var f;
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
}

class BlackHole extends HeapObj {}
