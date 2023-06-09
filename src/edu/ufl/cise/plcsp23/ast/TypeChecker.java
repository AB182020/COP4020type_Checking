package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.*;

import java.util.HashMap;

public class TypeChecker  implements ASTVisitor
{


    public TypeChecker()
    {
        ASTVisitor ast;
//        this.symbolTable.insert(ast.toString(),ast.visitDeclaration())
    }


    public static class SymbolTable {

        HashMap<String,Declaration> entries = new HashMap<>();

        //returns true if name successfully inserted in symbol table, false if already present
        public  boolean insert(String name, Declaration declaration) {
            return (entries.putIfAbsent(name,declaration) == null);
        }

        //returns Declaration if present, or null if name not declared.
        public Declaration lookup(String name) {
            return entries.get(name);
        }
    }
    SymbolTable symbolTable = new SymbolTable();





    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException
    {
        String name = statementAssign.toString();
        Declaration dec = symbolTable.lookup(name);
        checkNull(dec != null,  "undefined identifier " + name);
        checkAssigned(dec,  "using uninitialized variable");
//        identExpr.setDec(dec);  //save declaration--will be useful later.
        Type type = dec.nameDef.getType();

        return type;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException
    {
        Declaration dec;
       if(binaryExpr.right.firstToken.getKind() == IToken.Kind.IDENT)
       {
        dec = symbolTable.lookup(binaryExpr.right.firstToken.getTokenString());
        if(dec==null)
        {
            throw new TypeCheckException("Uninitalized Variable");
        }
       }
        if(binaryExpr.left.firstToken.getKind() == IToken.Kind.IDENT)
        {
            dec = symbolTable.lookup(binaryExpr.left.firstToken.getTokenString());
            if(dec==null)
            {
                throw new TypeCheckException("Uninitalized Variable");
            }
        }
       return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException
    {
        int declSize = block.decList.size();
        Declaration decStatement;
        for(int i=0; i< declSize; i++)
        {
            Declaration  decLookup = symbolTable.lookup(block.decList.get(i).nameDef.ident.getName());
            if(decLookup != null)
            {
                throw new TypeCheckException("Parameter already defined");
            }
            if(block.decList.get(i).initializer instanceof BinaryExpr)
            {
              visitBinaryExpr((BinaryExpr) block.decList.get(i).initializer,null);
            }
            if(block.decList.get(i).initializer instanceof IdentExpr)
            {
                visitIdentExpr((IdentExpr) block.decList.get(i).initializer,null);
            }
            decStatement = new Declaration( block.decList.get(i).firstToken,block.decList.get(i).nameDef,block.decList.get(i).initializer);
            symbolTable.insert(block.decList.get(i).nameDef.ident.getName(),decStatement);
        }
        int stmSize = block.statementList.size();
        Statement stm;

        for(int i=0; i<stmSize; i++)
        {
            if(block.statementList.get(i).firstToken.getKind() == IToken.Kind.COLON)
            {
                throw new TypeCheckException("Error Invalid Statement");
            }
//            Declaration  decLookup = symbolTable.lookup(block.decList.get(i).nameDef.ident.getName());
//            if(decLookup != null)
//            {
//                throw new TypeCheckException("Parameter already defined");
//            }
//            decStatement = new Declaration( block.decList.get(i).firstToken,block.decList.get(i).nameDef,block.decList.get(i).initializer);
//            symbolTable.insert(block.decList.get(i).nameDef.ident.getName(),decStatement);
        }
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException
    {
        String name = ident.getName();
        Declaration dec = symbolTable.lookup(name);
        if(dec == null)
        {
            throw  new SyntaxException("Error Undidentified ident " + name);
        }
        Type type = dec.nameDef.getType();
        return type;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException
    {
        String name = identExpr.getName();
        Declaration dec = symbolTable.lookup(name);
       checkNull(dec != null,  "undefined identifier " + name);
        checkAssigned(dec,  "using uninitialized variable");
//        identExpr.setDec(dec);  //save declaration--will be useful later.
        Type type = dec.nameDef.getType();
        identExpr.setType(type);
        return type;
    }

    private void checkAssigned(Declaration dec, String usingUninitializedVariable) throws SyntaxException {
        boolean isAssigned = false;
       Expr e = dec.getInitializer();
       if(e == null)
       {
           throw new SyntaxException(usingUninitializedVariable);
       }

    }

    public void addEntry(String s,Declaration dec )
    {
        symbolTable.insert(s,dec);
    }

    private void checkNull(boolean b, String s) throws SyntaxException {
        if(b != true)
        {
            throw new SyntaxException(s);
        }



}

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException
    {
        numLitExpr.setType(Type.INT);
        return Type.INT;

    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        Declaration decParam = null;
        Declaration dec = symbolTable.lookup(program.ident.getName());
        NameDef nameDf = new NameDef(program.firstToken ,program.type,null,program.getIdent());
        if(dec != null)
        {
            throw new SyntaxException("Program name already defined");
        }
        dec = new Declaration(program.firstToken,nameDf,null);
        symbolTable.insert(program.ident.getName(), null);
        int prmListLength =program.paramList.size();
        for(int i=0; i<prmListLength; i++)
        {
          Declaration  decLookup = symbolTable.lookup(program.paramList.get(i).ident.getName());
            if(decLookup != null)
            {
                throw new TypeCheckException("Parameter already defined");
            }
            if(program.paramList.get(i).type != Type.INT &&program.paramList.get(i).type != Type.STRING && program.paramList.get(i).type != Type.IMAGE&& program.paramList.get(i).type != Type.PIXEL )
            {
                throw new TypeCheckException("Incorrect type for parameter");
            }
            decParam = new Declaration( program.paramList.get(i).firstToken,program.paramList.get(i),null);
            symbolTable.insert(program.paramList.get(i).ident.getName(),decParam);
        }
        if(program.block.decList.size() != 0 || program.block.statementList.size() !=0)
        {
            visitBlock(program.block,null);
        }
        return symbolTable;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException
    {
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return null;
    }
}
