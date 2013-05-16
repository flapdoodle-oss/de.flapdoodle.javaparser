/**
 * Copyright (C) 2013
 *   Michael Mosmann <michael@mosmann.de>
 *
 * with contributions from
 * 	-
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//===========================================================================
//
//  Parsing Expression Grammar for Java 1.6 as a parboiled parser.
//  Based on Chapters 3 and 18 of Java Language Specification, Third Edition (JLS)
//  at http://java.sun.com/docs/books/jls/third_edition/html/j3TOC.html.
//
//---------------------------------------------------------------------------
//
//  Copyright (C) 2010 by Mathias Doenitz
//  Based on the Mouse 1.3 grammar for Java 1.6, which is
//  Copyright (C) 2006, 2009, 2010, 2011 by Roman R Redziejowski (www.romanredz.se).
//
//  The author gives unlimited permission to copy and distribute
//  this file, with or without modifications, as long as this notice
//  is preserved, and any changes are properly documented.
//
//  This file is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//---------------------------------------------------------------------------
//
//  Change log
//    2006-12-06 Posted on Internet.
//    2009-04-04 Modified to conform to Mouse syntax:
//               Underscore removed from names
//               \f in Space replaced by Unicode for FormFeed.
//    2009-07-10 Unused rule THREADSAFE removed.
//    2009-07-10 Copying and distribution conditions relaxed by the author.
//    2010-01-28 Transcribed to parboiled
//    2010-02-01 Fixed problem in rule "FormalParameterDecls"
//    2010-03-29 Fixed problem in "annotation"
//    2010-03-31 Fixed problem in unicode escapes, String literals and line comments
//               (Thanks to Reinier Zwitserloot for the finds)
//    2010-07-26 Fixed problem in LocalVariableDeclarationStatement (accept annotations),
//               HexFloat (HexSignificant) and AnnotationTypeDeclaration (bug in the JLS!)
//    2010-10-07 Added full support of Unicode Identifiers as set forth in the JLS
//               (Thanks for Ville Peurala for the patch)
//    2011-07-23 Transcribed all missing fixes from Romans Mouse grammar (http://www.romanredz.se/papers/Java.1.6.peg)
//
//===========================================================================

package de.flapdoodle.javaparser.parboiled;

import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.DontLabel;
import org.parboiled.annotations.MemoMismatches;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.StringVar;
import org.parboiled.support.Var;

import sun.awt.SubRegionShowable;

import com.google.common.collect.Lists;

import de.flapdoodle.javaparser.parboiled.helper.AbstractParameter;
import de.flapdoodle.javaparser.parboiled.helper.EmptyParameter;
import de.flapdoodle.javaparser.parboiled.helper.ParameterWithChild;
import de.flapdoodle.javaparser.tree.AbstractType;
import de.flapdoodle.javaparser.tree.Import;
import de.flapdoodle.javaparser.tree.JavaPackage;
import de.flapdoodle.javaparser.tree.Marker;
import de.flapdoodle.javaparser.tree.MemberDeclaration;
import de.flapdoodle.javaparser.tree.MethodDeclaration;
import de.flapdoodle.javaparser.tree.Source;

@SuppressWarnings({"InfiniteRecursion"})
//@BuildParseTree
public class JavaParser extends BaseParser<Object> {

	public static com.google.common.base.Optional<Source> asSource(String source) {
		ParsingResult<Source> result = parse(source);
		if (!result.hasErrors()) {
			return com.google.common.base.Optional.of(result.resultValue);
		}
		return com.google.common.base.Optional.absent();
	}
	
	private static ParsingResult<Source> parse(String source) {
		JavaParser parser = Parboiled.createParser(JavaParser.class);
		Rule rootRule = parser.CompilationUnit();
		return new ReportingParseRunner<Source>(rootRule).run(source);
	}
	
	protected <T> T as(Object ret,Class<T> type) {
		if (type.isInstance(ret)) {
//			System.out.println(">>>"+ret);
			return (T) ret;
		}
		throw new IllegalArgumentException("Does not match " + type+" ("+ret+")");
	}
	
	protected Marker marker() {
		return new Marker(getContext().getStartIndex(),currentIndex());
	}
	
	protected static boolean noMatch(boolean whatEver) {
		return false;
	}
	

    //-------------------------------------------------------------------------
    //  Compilation Unit
    //-------------------------------------------------------------------------

    public Rule CompilationUnit() {
    	Var<JavaPackage> javaPackage=new Var<JavaPackage>();
    	CollectionVar<Import> imports=new CollectionVar<>();
    	CollectionVar<AbstractType> types=new CollectionVar<>();
        return Sequence(
        				Spacing(),
                Optional(PackageDeclaration(javaPackage)),
                ZeroOrMore(ImportDeclaration(imports)),
                ZeroOrMore(TypeDeclaration(types)),
                EOI,push(new Source(marker(),javaPackage.get(),imports.asList(),types.asList()))
        );
    }

    Rule PackageDeclaration(Var<JavaPackage> javaPackage) {
    	Var<String> packageName=new Var<>();
        return Sequence(ZeroOrMore(Annotation()), Sequence(PACKAGE, QualifiedIdentifier(), packageName.set(match()), SEMI),javaPackage.set(new JavaPackage(marker(),packageName.get())));
    }

    Rule ImportDeclaration(CollectionVar<Import> imports) {
    	Var<String> staticKey=new Var<>();
    	StringVar importDecl=new StringVar();
        return Sequence(
                IMPORT,
                Optional(STATIC),staticKey.set(match()),
                QualifiedIdentifier(),
                importDecl.append(match()),
                Optional(DOT, STAR),
                importDecl.append(match()),
                SEMI,
                imports.add(new Import(marker(),!"".equals(staticKey.get()),importDecl.get()))
        );
    }

    Rule TypeDeclaration(CollectionVar<AbstractType> types) {
        return FirstOf(
                  FirstOf(
                          ClassDeclaration(types),
                          EnumDeclaration(types),
                          InterfaceTypeDeclaration(types),
                          AnnotationTypeDeclaration(types)
                  )
                ,
                SEMI
        );
    }

    //-------------------------------------------------------------------------
    //  Class Declaration
    //-------------------------------------------------------------------------

    Rule ClassDeclaration(CollectionVar<AbstractType> types) {
    	CollectionVar<MemberDeclaration> subTypes=new CollectionVar<>();
    	Var<String> id=new Var<>();
    	  return Sequence(
        				ZeroOrMore(Modifier()),
                CLASS,
                Identifier(),
                id.set(match()),
                Optional(TypeParameters()),
                Optional(EXTENDS, ClassType()),
                Optional(IMPLEMENTS, ClassTypeList()),
                ClassBody(subTypes),
                types.add(new de.flapdoodle.javaparser.tree.ClassType(marker(),id.get(),subTypes.asList()))
        );
    }

    Rule ClassBody(CollectionVar<MemberDeclaration> memberDecls) {
        return Sequence(LWING, ZeroOrMore(ClassBodyDeclaration(memberDecls)), RWING);
    }

    Rule ClassBodyDeclaration(CollectionVar<MemberDeclaration> memberDecls) {
        return FirstOf(
                SEMI,
                Sequence(Optional(STATIC), Block()),
                Sequence(MemberDecl(),memberDecls.add(as(pop(),MemberDeclaration.class)))
        );
    }

    Rule MemberDecl() {
    	CollectionVar<AbstractType> subTypes=new CollectionVar<>();
    	CollectionVar<MethodDeclaration> methodDeclarations=new CollectionVar<>();
        return Sequence(
        				FirstOf(
                Sequence(ZeroOrMore(Modifier()), TypeParameters(), GenericMethodOrConstructorRest()),
                MethodDecl(methodDeclarations),
                Sequence(ZeroOrMore(Modifier()), Type(), VariableDeclarators(), SEMI),
                VoidMethodDecl(methodDeclarations),
                Sequence(ZeroOrMore(Modifier()), Identifier(), ConstructorDeclaratorRest()),
                InterfaceDeclarationWithModifier(new Var<String>()),
                ClassDeclaration(subTypes),
                EnumDeclaration(subTypes),
                AnnotationTypeDeclaration(subTypes)),
                push(new MemberDeclaration(marker(),subTypes.asList(),methodDeclarations.asList()))
        );
    }

		Rule VoidMethodDecl(CollectionVar<MethodDeclaration> methodDeclarations) {
			Var<AbstractParameter> parameters=new Var<AbstractParameter>(new EmptyParameter());
			Var<String> id=new Var<>();
			return Sequence(ZeroOrMore(Modifier()), VOID, Identifier(), id.set(match()), VoidMethodDeclaratorRest(parameters),methodDeclarations.add(new MethodDeclaration(marker(),id.get(),parameters.get().asParameterList())));
		}

		Rule MethodDecl(CollectionVar<MethodDeclaration> methodDeclarations) {
			Var<AbstractParameter> parameters=new Var<AbstractParameter>(new EmptyParameter());
			Var<String> id=new Var<>();
			return Sequence(ZeroOrMore(Modifier()), Type(), Identifier(), id.set(match()), MethodDeclaratorRest(parameters),methodDeclarations.add(new MethodDeclaration(marker(),id.get(),parameters.get().asParameterList())));
		}

    Rule GenericMethodOrConstructorRest() {
        return FirstOf(
                Sequence(FirstOf(Type(), VOID), Identifier(), MethodDeclaratorRest()),
                Sequence(Identifier(), ConstructorDeclaratorRest())
        );
    }

    Rule MethodDeclaratorRest() {
    	return MethodDeclaratorRest(new Var<AbstractParameter>());
    }
    
    Rule MethodDeclaratorRest(Var<AbstractParameter> parameters) {
        return Sequence(
                FormalParameters(parameters),
                ZeroOrMore(Dim()),
                Optional(THROWS, ClassTypeList()),
                FirstOf(MethodBody(), SEMI)
        );
    }

    Rule VoidMethodDeclaratorRest(Var<AbstractParameter> parameters) {
        return Sequence(
                FormalParameters(parameters),
                Optional(THROWS, ClassTypeList()),
                FirstOf(MethodBody(), SEMI)
        );
    }

    Rule ConstructorDeclaratorRest() {
        return Sequence(FormalParameters(), Optional(THROWS, ClassTypeList()), MethodBody());
    }

    Rule MethodBody() {
        return Block();
    }

    //-------------------------------------------------------------------------
    //  Interface Declaration
    //-------------------------------------------------------------------------

    Rule InterfaceTypeDeclaration(CollectionVar<AbstractType> types) {
    	Var<String> name=new Var<String>();
    	return Sequence(ZeroOrMore(Modifier()), InterfaceDeclaration(name),
          types.add(new de.flapdoodle.javaparser.tree.InterfaceType(marker(),name.get())));
    }
    
    Rule InterfaceDeclarationWithModifier(Var<String> name) {
    	return Sequence(ZeroOrMore(Modifier()), InterfaceDeclaration(name));
    }
    
    Rule InterfaceDeclaration(Var<String> name) {
        return Sequence(
                INTERFACE,
                Identifier(),
                name.set(match()),
                Optional(TypeParameters()),
                Optional(EXTENDS, ClassTypeList()),
                InterfaceBody()
        );
    }

    Rule InterfaceBody() {
        return Sequence(LWING, ZeroOrMore(InterfaceBodyDeclaration()), RWING);
    }

    Rule InterfaceBodyDeclaration() {
        return FirstOf(
                Sequence(ZeroOrMore(Modifier()), InterfaceMemberDecl()),
                SEMI
        );
    }

    Rule InterfaceMemberDecl() {
    	CollectionVar<AbstractType> dummyType=new CollectionVar<>();
        return FirstOf(
                InterfaceMethodOrFieldDecl(),
                InterfaceGenericMethodDecl(),
                Sequence(VOID, Identifier(), VoidInterfaceMethodDeclaratorsRest()),
                InterfaceDeclaration(new Var<String>()),
                AnnotationTypeDeclaration(dummyType),
                ClassDeclaration(dummyType),
                EnumDeclaration(dummyType)
        );
    }

    Rule InterfaceMethodOrFieldDecl() {
        return Sequence(Sequence(Type(), Identifier()), InterfaceMethodOrFieldRest());
    }

    Rule InterfaceMethodOrFieldRest() {
        return FirstOf(
                Sequence(ConstantDeclaratorsRest(), SEMI),
                InterfaceMethodDeclaratorRest()
        );
    }

    Rule InterfaceMethodDeclaratorRest() {
        return Sequence(
                FormalParameters(),
                ZeroOrMore(Dim()),
                Optional(THROWS, ClassTypeList()),
                SEMI
        );
    }

    Rule InterfaceGenericMethodDecl() {
        return Sequence(TypeParameters(), FirstOf(Type(), VOID), Identifier(), InterfaceMethodDeclaratorRest());
    }

    Rule VoidInterfaceMethodDeclaratorsRest() {
        return Sequence(FormalParameters(), Optional(THROWS, ClassTypeList()), SEMI);
    }

    Rule ConstantDeclaratorsRest() {
        return Sequence(ConstantDeclaratorRest(), ZeroOrMore(COMMA, ConstantDeclarator()));
    }

    Rule ConstantDeclarator() {
        return Sequence(Identifier(), ConstantDeclaratorRest());
    }

    Rule ConstantDeclaratorRest() {
        return Sequence(ZeroOrMore(Dim()), EQU, VariableInitializer());
    }

    //-------------------------------------------------------------------------
    //  Enum Declaration
    //-------------------------------------------------------------------------

    Rule EnumDeclaration(CollectionVar<AbstractType> types) {
        return Sequence(
        				ZeroOrMore(Modifier()),
                ENUM,
                Identifier(),
                push(match()),
                Optional(IMPLEMENTS, ClassTypeList()),
                EnumBody(),
                types.add(new de.flapdoodle.javaparser.tree.EnumType(marker(),as(pop(),String.class),Lists.<MemberDeclaration>newArrayList()))
        );
    }

    Rule EnumBody() {
        return Sequence(
                LWING,
                Optional(EnumConstants()),
                Optional(COMMA),
                Optional(EnumBodyDeclarations()),
                RWING
        );
    }

    Rule EnumConstants() {
        return Sequence(EnumConstant(), ZeroOrMore(COMMA, EnumConstant()));
    }

    Rule EnumConstant() {
    	// TODO can we address enum const sub types
    	CollectionVar<MemberDeclaration> enumConstMemberDecls=new CollectionVar<>();
        return Sequence(
                ZeroOrMore(Annotation()),
                Identifier(),
                Optional(Arguments()),
                Optional(ClassBody(enumConstMemberDecls))
        );
    }

    Rule EnumBodyDeclarations() {
    	// TODO whats enum body type declarations?
        CollectionVar<MemberDeclaration> enumBodyMemberDecl=new CollectionVar<>();
				return Sequence(SEMI, ZeroOrMore(ClassBodyDeclaration(enumBodyMemberDecl)));
    }

    //-------------------------------------------------------------------------
    //  Variable Declarations
    //-------------------------------------------------------------------------    

    Rule LocalVariableDeclarationStatement() {
        return Sequence(ZeroOrMore(FirstOf(FINAL, Annotation())), Type(), VariableDeclarators(), SEMI);
    }

    Rule VariableDeclarators() {
        return Sequence(VariableDeclarator(), ZeroOrMore(COMMA, VariableDeclarator()));
    }

    Rule VariableDeclarator() {
        return Sequence(Identifier(), ZeroOrMore(Dim()), Optional(EQU, VariableInitializer()));
    }

    //-------------------------------------------------------------------------
    //  Formal Parameters
    //-------------------------------------------------------------------------

    Rule FormalParameters() {
    	return FormalParameters(new Var<AbstractParameter>());
    }
    
    Rule FormalParameters(Var<AbstractParameter> parameter) {
        return Sequence(LPAR, Optional(FormalParameterDecls(),parameter.set(as(pop(),AbstractParameter.class))), RPAR);
    }

    Rule FormalParameter() {
        return Sequence(ZeroOrMore(FirstOf(FINAL, Annotation())), Type(), VariableDeclaratorId());
    }

    Rule FormalParameterDecls() {
    	Var<String> typeAsString=new Var<>();
        return Sequence(ZeroOrMore(FirstOf(FINAL, Annotation())), Type(),typeAsString.set(match()), FormalParameterDeclsRest(),push(new ParameterWithChild(typeAsString.get(), as(pop(),AbstractParameter.class))));
    }

    Rule FormalParameterDeclsRest() {
    	Var<AbstractParameter> toPush=new Var<AbstractParameter>(new EmptyParameter());
        return Sequence(
        				FirstOf(
                Sequence(VariableDeclaratorId(), Optional(COMMA, FormalParameterDecls(),toPush.set(as(pop(),AbstractParameter.class)))),
                Sequence(ELLIPSIS, VariableDeclaratorId())),
                push(toPush.get())
        );
    }

    Rule VariableDeclaratorId() {
        return Sequence(Identifier(), ZeroOrMore(Dim()));
    }

    //-------------------------------------------------------------------------
    //  Statements
    //-------------------------------------------------------------------------    

    Rule Block() {
        return Sequence(LWING, BlockStatements(), RWING);
    }

    Rule BlockStatements() {
        return ZeroOrMore(BlockStatement());
    }

    Rule BlockStatement() {
    	CollectionVar<AbstractType> dummyType=new CollectionVar<>();
        return FirstOf(
                LocalVariableDeclarationStatement(),
                Sequence(ZeroOrMore(Modifier()), FirstOf(ClassDeclaration(dummyType), EnumDeclaration(dummyType))),
                Statement()
        );
    }

    Rule Statement() {
        return FirstOf(
                Block(),
                Sequence(ASSERT, Expression(), Optional(COLON, Expression()), SEMI),
                Sequence(IF, ParExpression(), Statement(), Optional(ELSE, Statement())),
                Sequence(FOR, LPAR, Optional(ForInit()), SEMI, Optional(Expression()), SEMI, Optional(ForUpdate()),
                        RPAR, Statement()),
                Sequence(FOR, LPAR, FormalParameter(), COLON, Expression(), RPAR, Statement()),
                Sequence(WHILE, ParExpression(), Statement()),
                Sequence(DO, Statement(), WHILE, ParExpression(), SEMI),
                Sequence(TRY, Block(),
                        FirstOf(Sequence(OneOrMore(Catch_()), Optional(Finally_())), Finally_())),
                Sequence(SWITCH, ParExpression(), LWING, SwitchBlockStatementGroups(), RWING),
                Sequence(SYNCHRONIZED, ParExpression(), Block()),
                Sequence(RETURN, Optional(Expression()), SEMI),
                Sequence(THROW, Expression(), SEMI),
                Sequence(BREAK, Optional(Identifier()), SEMI),
                Sequence(CONTINUE, Optional(Identifier()), SEMI),
                Sequence(Sequence(Identifier(), COLON), Statement()),
                Sequence(StatementExpression(), SEMI),
                SEMI
        );
    }

    Rule Catch_() {
        return Sequence(CATCH, LPAR, FormalParameter(), RPAR, Block());
    }

    Rule Finally_() {
        return Sequence(FINALLY, Block());
    }

    Rule SwitchBlockStatementGroups() {
        return ZeroOrMore(SwitchBlockStatementGroup());
    }

    Rule SwitchBlockStatementGroup() {
        return Sequence(SwitchLabel(), BlockStatements());
    }

    Rule SwitchLabel() {
        return FirstOf(
                Sequence(CASE, ConstantExpression(), COLON),
                Sequence(CASE, EnumConstantName(), COLON),
                Sequence(DEFAULT, COLON)
        );
    }

    Rule ForInit() {
        return FirstOf(
                Sequence(ZeroOrMore(FirstOf(FINAL, Annotation())), Type(), VariableDeclarators()),
                Sequence(StatementExpression(), ZeroOrMore(COMMA, StatementExpression()))
        );
    }

    Rule ForUpdate() {
        return Sequence(StatementExpression(), ZeroOrMore(COMMA, StatementExpression()));
    }

    Rule EnumConstantName() {
        return Identifier();
    }

    //-------------------------------------------------------------------------
    //  Expressions
    //-------------------------------------------------------------------------

    // The following is more generous than the definition in section 14.8,
    // which allows only specific forms of Expression.

    Rule StatementExpression() {
        return Expression();
    }

    Rule ConstantExpression() {
        return Expression();
    }

    // The following definition is part of the modification in JLS Chapter 18
    // to minimize look ahead. In JLS Chapter 15.27, Expression is defined
    // as AssignmentExpression, which is effectively defined as
    // (LeftHandSide AssignmentOperator)* ConditionalExpression.
    // The following is obtained by allowing ANY ConditionalExpression
    // as LeftHandSide, which results in accepting statements like 5 = a.

    Rule Expression() {
        return Sequence(
                ConditionalExpression(),
                ZeroOrMore(AssignmentOperator(), ConditionalExpression())
        );
    }

    Rule AssignmentOperator() {
        return FirstOf(EQU, PLUSEQU, MINUSEQU, STAREQU, DIVEQU, ANDEQU, OREQU, HATEQU, MODEQU, SLEQU, SREQU, BSREQU);
    }

    Rule ConditionalExpression() {
        return Sequence(
                ConditionalOrExpression(),
                ZeroOrMore(QUERY, Expression(), COLON, ConditionalOrExpression())
        );
    }

    Rule ConditionalOrExpression() {
        return Sequence(
                ConditionalAndExpression(),
                ZeroOrMore(OROR, ConditionalAndExpression())
        );
    }

    Rule ConditionalAndExpression() {
        return Sequence(
                InclusiveOrExpression(),
                ZeroOrMore(ANDAND, InclusiveOrExpression())
        );
    }

    Rule InclusiveOrExpression() {
        return Sequence(
                ExclusiveOrExpression(),
                ZeroOrMore(OR, ExclusiveOrExpression())
        );
    }

    Rule ExclusiveOrExpression() {
        return Sequence(
                AndExpression(),
                ZeroOrMore(HAT, AndExpression())
        );
    }

    Rule AndExpression() {
        return Sequence(
                EqualityExpression(),
                ZeroOrMore(AND, EqualityExpression())
        );
    }

    Rule EqualityExpression() {
        return Sequence(
                RelationalExpression(),
                ZeroOrMore(FirstOf(EQUAL, NOTEQUAL), RelationalExpression())
        );
    }

    Rule RelationalExpression() {
        return Sequence(
                ShiftExpression(),
                ZeroOrMore(
                        FirstOf(
                                Sequence(FirstOf(LE, GE, LT, GT), ShiftExpression()),
                                Sequence(INSTANCEOF, ReferenceType())
                        )
                )
        );
    }

    Rule ShiftExpression() {
        return Sequence(
                AdditiveExpression(),
                ZeroOrMore(FirstOf(SL, SR, BSR), AdditiveExpression())
        );
    }

    Rule AdditiveExpression() {
        return Sequence(
                MultiplicativeExpression(),
                ZeroOrMore(FirstOf(PLUS, MINUS), MultiplicativeExpression())
        );
    }

    Rule MultiplicativeExpression() {
        return Sequence(
                UnaryExpression(),
                ZeroOrMore(FirstOf(STAR, DIV, MOD), UnaryExpression())
        );
    }

    Rule UnaryExpression() {
        return FirstOf(
                Sequence(PrefixOp(), UnaryExpression()),
                Sequence(LPAR, Type(), RPAR, UnaryExpression()),
                Sequence(Primary(), ZeroOrMore(Selector()), ZeroOrMore(PostFixOp()))
        );
    }

    Rule Primary() {
        return FirstOf(
                ParExpression(),
                Sequence(
                        NonWildcardTypeArguments(),
                        FirstOf(ExplicitGenericInvocationSuffix(), Sequence(THIS, Arguments()))
                ),
                Sequence(THIS, Optional(Arguments())),
                Sequence(SUPER, SuperSuffix()),
                Literal(),
                Sequence(NEW, Creator()),
                Sequence(QualifiedIdentifier(), Optional(IdentifierSuffix())),
                Sequence(BasicType(), ZeroOrMore(Dim()), DOT, CLASS),
                Sequence(VOID, DOT, CLASS)
        );
    }

    Rule IdentifierSuffix() {
        return FirstOf(
                Sequence(LBRK,
                        FirstOf(
                                Sequence(RBRK, ZeroOrMore(Dim()), DOT, CLASS),
                                Sequence(Expression(), RBRK)
                        )
                ),
                Arguments(),
                Sequence(
                        DOT,
                        FirstOf(
                                CLASS,
                                ExplicitGenericInvocation(),
                                THIS,
                                Sequence(SUPER, Arguments()),
                                Sequence(NEW, Optional(NonWildcardTypeArguments()), InnerCreator())
                        )
                )
        );
    }

    Rule ExplicitGenericInvocation() {
        return Sequence(NonWildcardTypeArguments(), ExplicitGenericInvocationSuffix());
    }

    Rule NonWildcardTypeArguments() {
        return Sequence(LPOINT, ReferenceType(), ZeroOrMore(COMMA, ReferenceType()), RPOINT);
    }

    Rule ExplicitGenericInvocationSuffix() {
        return FirstOf(
                Sequence(SUPER, SuperSuffix()),
                Sequence(Identifier(), Arguments())
        );
    }

    Rule PrefixOp() {
        return FirstOf(INC, DEC, BANG, TILDA, PLUS, MINUS);
    }

    Rule PostFixOp() {
        return FirstOf(INC, DEC);
    }

    Rule Selector() {
        return FirstOf(
                Sequence(DOT, Identifier(), Optional(Arguments())),
                Sequence(DOT, ExplicitGenericInvocation()),
                Sequence(DOT, THIS),
                Sequence(DOT, SUPER, SuperSuffix()),
                Sequence(DOT, NEW, Optional(NonWildcardTypeArguments()), InnerCreator()),
                DimExpr()
        );
    }

    Rule SuperSuffix() {
        return FirstOf(Arguments(), Sequence(DOT, Identifier(), Optional(Arguments())));
    }

    @MemoMismatches
    Rule BasicType() {
        return Sequence(
                FirstOf("byte", "short", "char", "int", "long", "float", "double", "boolean"),
                TestNot(LetterOrDigit()),
                Spacing()
        );
    }

    Rule Arguments() {
        return Sequence(
                LPAR,
                Optional(Expression(), ZeroOrMore(COMMA, Expression())),
                RPAR
        );
    }

    Rule Creator() {
        return FirstOf(
                Sequence(Optional(NonWildcardTypeArguments()), CreatedName(), ClassCreatorRest()),
                Sequence(Optional(NonWildcardTypeArguments()), FirstOf(ClassType(), BasicType()), ArrayCreatorRest())
        );
    }

    Rule CreatedName() {
        return Sequence(
                Identifier(), Optional(NonWildcardTypeArguments()),
                ZeroOrMore(DOT, Identifier(), Optional(NonWildcardTypeArguments()))
        );
    }

    Rule InnerCreator() {
        return Sequence(Identifier(), ClassCreatorRest());
    }

    // The following is more generous than JLS 15.10. According to that definition,
    // BasicType must be followed by at least one DimExpr or by ArrayInitializer.
    Rule ArrayCreatorRest() {
        return Sequence(
                LBRK,
                FirstOf(
                        Sequence(RBRK, ZeroOrMore(Dim()), ArrayInitializer()),
                        Sequence(Expression(), RBRK, ZeroOrMore(DimExpr()), ZeroOrMore(Dim()))
                )
        );
    }

    Rule ClassCreatorRest() {
    	// TODO we ignore anon method level class def
        return Sequence(Arguments(), Optional(ClassBody(new CollectionVar<MemberDeclaration>())));
    }

    Rule ArrayInitializer() {
        return Sequence(
                LWING,
                Optional(
                    VariableInitializer(),
                    ZeroOrMore(COMMA, VariableInitializer())
                ),
                Optional(COMMA),
                RWING
        );
    }

    Rule VariableInitializer() {
        return FirstOf(ArrayInitializer(), Expression());
    }

    Rule ParExpression() {
        return Sequence(LPAR, Expression(), RPAR);
    }

    Rule QualifiedIdentifier() {
        return Sequence(Identifier(), ZeroOrMore(DOT, Identifier()));
    }

    Rule Dim() {
        return Sequence(LBRK, RBRK);
    }

    Rule DimExpr() {
        return Sequence(LBRK, Expression(), RBRK);
    }

    //-------------------------------------------------------------------------
    //  Types and Modifiers
    //-------------------------------------------------------------------------

    Rule Type() {
        return Sequence(FirstOf(BasicType(), ClassType()), ZeroOrMore(Dim()));
    }

    Rule ReferenceType() {
        return FirstOf(
                Sequence(BasicType(), OneOrMore(Dim())),
                Sequence(ClassType(), ZeroOrMore(Dim()))
        );
    }

    Rule ClassType() {
        return Sequence(
                Identifier(), Optional(TypeArguments()),
                ZeroOrMore(DOT, Identifier(), Optional(TypeArguments()))
        );
    }

    Rule ClassTypeList() {
        return Sequence(ClassType(), ZeroOrMore(COMMA, ClassType()));
    }

    Rule TypeArguments() {
        return Sequence(LPOINT, TypeArgument(), ZeroOrMore(COMMA, TypeArgument()), RPOINT);
    }

    Rule TypeArgument() {
        return FirstOf(
                ReferenceType(),
                Sequence(QUERY, Optional(FirstOf(EXTENDS, SUPER), ReferenceType()))
        );
    }

    Rule TypeParameters() {
        return Sequence(LPOINT, TypeParameter(), ZeroOrMore(COMMA, TypeParameter()), RPOINT);
    }

    Rule TypeParameter() {
        return Sequence(Identifier(), Optional(EXTENDS, Bound()));
    }

    Rule Bound() {
        return Sequence(ClassType(), ZeroOrMore(AND, ClassType()));
    }

    // the following common definition of Modifier is part of the modification
    // in JLS Chapter 18 to minimize look ahead. The main body of JLS has
    // different lists of modifiers for different language elements.
    Rule Modifier() {
        return FirstOf(
                Annotation(),
                Sequence(
                        FirstOf("public", "protected", "private", "static", "abstract", "final", "native",
                                "synchronized", "transient", "volatile", "strictfp"),
                        TestNot(LetterOrDigit()),
                        Spacing()
                )
        );
    }

    //-------------------------------------------------------------------------
    //  Annotations
    //-------------------------------------------------------------------------    

    Rule AnnotationTypeDeclaration(CollectionVar<AbstractType> types) {
        return Sequence(ZeroOrMore(Modifier()), AT, INTERFACE, Identifier(),push(match()), AnnotationTypeBody(),types.add(new de.flapdoodle.javaparser.tree.AnnotationType(marker(),as(pop(),String.class))));
    }

    Rule AnnotationTypeBody() {
        return Sequence(LWING, ZeroOrMore(AnnotationTypeElementDeclaration()), RWING);
    }

    Rule AnnotationTypeElementDeclaration() {
        return FirstOf(
                Sequence(ZeroOrMore(Modifier()), AnnotationTypeElementRest()),
                SEMI
        );
    }

    Rule AnnotationTypeElementRest() {
    	CollectionVar<AbstractType> dummyType=new CollectionVar<>();
        return FirstOf(
                Sequence(Type(), AnnotationMethodOrConstantRest(), SEMI),
                ClassDeclaration(dummyType),
                EnumDeclaration(dummyType),
                InterfaceDeclaration(new Var<String>()),
                AnnotationTypeDeclaration(dummyType)
        );
    }

    Rule AnnotationMethodOrConstantRest() {
        return FirstOf(AnnotationMethodRest(), AnnotationConstantRest());
    }

    Rule AnnotationMethodRest() {
        return Sequence(Identifier(), LPAR, RPAR, Optional(DefaultValue()));
    }

    Rule AnnotationConstantRest() {
        return VariableDeclarators();
    }

    Rule DefaultValue() {
        return Sequence(DEFAULT, ElementValue());
    }

    @MemoMismatches
    Rule Annotation() {
        return Sequence(AT, QualifiedIdentifier(), Optional(AnnotationRest()));
    }

    Rule AnnotationRest() {
        return FirstOf(NormalAnnotationRest(), SingleElementAnnotationRest());
    }

    Rule NormalAnnotationRest() {
        return Sequence(LPAR, Optional(ElementValuePairs()), RPAR);
    }

    Rule ElementValuePairs() {
        return Sequence(ElementValuePair(), ZeroOrMore(COMMA, ElementValuePair()));
    }

    Rule ElementValuePair() {
        return Sequence(Identifier(), EQU, ElementValue());
    }

    Rule ElementValue() {
        return FirstOf(ConditionalExpression(), Annotation(), ElementValueArrayInitializer());
    }

    Rule ElementValueArrayInitializer() {
        return Sequence(LWING, Optional(ElementValues()), Optional(COMMA), RWING);
    }

    Rule ElementValues() {
        return Sequence(ElementValue(), ZeroOrMore(COMMA, ElementValue()));
    }

    Rule SingleElementAnnotationRest() {
        return Sequence(LPAR, ElementValue(), RPAR);
    }

    //-------------------------------------------------------------------------
    //  JLS 3.6-7  Spacing
    //-------------------------------------------------------------------------

    @SuppressNode
    Rule Spacing() {
        return ZeroOrMore(FirstOf(

                // whitespace
                OneOrMore(AnyOf(" \t\r\n\f").label("Whitespace")),

                // traditional comment
                Sequence("/*", ZeroOrMore(TestNot("*/"), ANY), "*/"),

                // end of line comment
                Sequence(
                        "//",
                        ZeroOrMore(TestNot(AnyOf("\r\n")), ANY),
                        FirstOf("\r\n", '\r', '\n', EOI)
                )
        ));
    }

    //-------------------------------------------------------------------------
    //  JLS 3.8  Identifiers
    //-------------------------------------------------------------------------

    @SuppressSubnodes
    @MemoMismatches
    Rule Identifier() {
        return Sequence(TestNot(Keyword()), Letter(), ZeroOrMore(LetterOrDigit()), Spacing());
    }

    // JLS defines letters and digits as Unicode characters recognized
    // as such by special Java procedures.

    Rule Letter() {
        // switch to this "reduced" character space version for a ~10% parser performance speedup
        //return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_', '$');
        return FirstOf(Sequence('\\', UnicodeEscape()), new JavaLetterMatcher());
    }

    @MemoMismatches
    Rule LetterOrDigit() {
        // switch to this "reduced" character space version for a ~10% parser performance speedup
        //return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_', '$');
        return FirstOf(Sequence('\\', UnicodeEscape()), new JavaLetterOrDigitMatcher());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.9  Keywords
    //-------------------------------------------------------------------------

    @MemoMismatches
    Rule Keyword() {
        return Sequence(
                FirstOf("assert", "break", "case", "catch", "class", "const", "continue", "default", "do", "else",
                        "enum", "extends", "finally", "final", "for", "goto", "if", "implements", "import", "interface",
                        "instanceof", "new", "package", "return", "static", "super", "switch", "synchronized", "this",
                        "throws", "throw", "try", "void", "while"),
                TestNot(LetterOrDigit())
        );
    }

    public final Rule ASSERT = Keyword("assert");
    public final Rule BREAK = Keyword("break");
    public final Rule CASE = Keyword("case");
    public final Rule CATCH = Keyword("catch");
    public final Rule CLASS = Keyword("class");
    public final Rule CONTINUE = Keyword("continue");
    public final Rule DEFAULT = Keyword("default");
    public final Rule DO = Keyword("do");
    public final Rule ELSE = Keyword("else");
    public final Rule ENUM = Keyword("enum");
    public final Rule EXTENDS = Keyword("extends");
    public final Rule FINALLY = Keyword("finally");
    public final Rule FINAL = Keyword("final");
    public final Rule FOR = Keyword("for");
    public final Rule IF = Keyword("if");
    public final Rule IMPLEMENTS = Keyword("implements");
    public final Rule IMPORT = Keyword("import");
    public final Rule INTERFACE = Keyword("interface");
    public final Rule INSTANCEOF = Keyword("instanceof");
    public final Rule NEW = Keyword("new");
    public final Rule PACKAGE = Keyword("package");
    public final Rule RETURN = Keyword("return");
    public final Rule STATIC = Keyword("static");
    public final Rule SUPER = Keyword("super");
    public final Rule SWITCH = Keyword("switch");
    public final Rule SYNCHRONIZED = Keyword("synchronized");
    public final Rule THIS = Keyword("this");
    public final Rule THROWS = Keyword("throws");
    public final Rule THROW = Keyword("throw");
    public final Rule TRY = Keyword("try");
    public final Rule VOID = Keyword("void");
    public final Rule WHILE = Keyword("while");

    @SuppressNode
    @DontLabel
    Rule Keyword(String keyword) {
        return Terminal(keyword, LetterOrDigit());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.10  Literals
    //-------------------------------------------------------------------------

    Rule Literal() {
        return Sequence(
                FirstOf(
                        FloatLiteral(),
                        IntegerLiteral(),
                        CharLiteral(),
                        StringLiteral(),
                        Sequence("true", TestNot(LetterOrDigit())),
                        Sequence("false", TestNot(LetterOrDigit())),
                        Sequence("null", TestNot(LetterOrDigit()))
                ),
                Spacing()
        );
    }

    @SuppressSubnodes
    Rule IntegerLiteral() {
        return Sequence(FirstOf(HexNumeral(), OctalNumeral(), DecimalNumeral()), Optional(AnyOf("lL")));
    }

    @SuppressSubnodes
    Rule DecimalNumeral() {
        return FirstOf('0', Sequence(CharRange('1', '9'), ZeroOrMore(Digit())));
    }

    @SuppressSubnodes

    @MemoMismatches
    Rule HexNumeral() {
        return Sequence('0', IgnoreCase('x'), OneOrMore(HexDigit()));
    }

    Rule HexDigit() {
        return FirstOf(CharRange('a', 'f'), CharRange('A', 'F'), CharRange('0', '9'));
    }

    @SuppressSubnodes
    Rule OctalNumeral() {
        return Sequence('0', OneOrMore(CharRange('0', '7')));
    }

    Rule FloatLiteral() {
        return FirstOf(HexFloat(), DecimalFloat());
    }

    @SuppressSubnodes
    Rule DecimalFloat() {
        return FirstOf(
                Sequence(OneOrMore(Digit()), '.', ZeroOrMore(Digit()), Optional(Exponent()), Optional(AnyOf("fFdD"))),
                Sequence('.', OneOrMore(Digit()), Optional(Exponent()), Optional(AnyOf("fFdD"))),
                Sequence(OneOrMore(Digit()), Exponent(), Optional(AnyOf("fFdD"))),
                Sequence(OneOrMore(Digit()), Optional(Exponent()), AnyOf("fFdD"))
        );
    }

    Rule Exponent() {
        return Sequence(AnyOf("eE"), Optional(AnyOf("+-")), OneOrMore(Digit()));
    }

    Rule Digit() {
        return CharRange('0', '9');
    }

    @SuppressSubnodes
    Rule HexFloat() {
        return Sequence(HexSignificant(), BinaryExponent(), Optional(AnyOf("fFdD")));
    }

    Rule HexSignificant() {
        return FirstOf(
                Sequence(FirstOf("0x", "0X"), ZeroOrMore(HexDigit()), '.', OneOrMore(HexDigit())),
                Sequence(HexNumeral(), Optional('.'))
        );
    }

    Rule BinaryExponent() {
        return Sequence(AnyOf("pP"), Optional(AnyOf("+-")), OneOrMore(Digit()));
    }

    Rule CharLiteral() {
        return Sequence(
                '\'',
                FirstOf(Escape(), Sequence(TestNot(AnyOf("'\\")), ANY)).suppressSubnodes(),
                '\''
        );
    }

    Rule StringLiteral() {
        return Sequence(
                '"',
                ZeroOrMore(
                        FirstOf(
                                Escape(),
                                Sequence(TestNot(AnyOf("\r\n\"\\")), ANY)
                        )
                ).suppressSubnodes(),
                '"'
        );
    }

    Rule Escape() {
        return Sequence('\\', FirstOf(AnyOf("btnfr\"\'\\"), OctalEscape(), UnicodeEscape()));
    }

    Rule OctalEscape() {
        return FirstOf(
                Sequence(CharRange('0', '3'), CharRange('0', '7'), CharRange('0', '7')),
                Sequence(CharRange('0', '7'), CharRange('0', '7')),
                CharRange('0', '7')
        );
    }

    Rule UnicodeEscape() {
        return Sequence(OneOrMore('u'), HexDigit(), HexDigit(), HexDigit(), HexDigit());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.11-12  Separators, Operators
    //-------------------------------------------------------------------------

    final Rule AT = Terminal("@");
    final Rule AND = Terminal("&", AnyOf("=&"));
    final Rule ANDAND = Terminal("&&");
    final Rule ANDEQU = Terminal("&=");
    final Rule BANG = Terminal("!", Ch('='));
    final Rule BSR = Terminal(">>>", Ch('='));
    final Rule BSREQU = Terminal(">>>=");
    final Rule COLON = Terminal(":");
    final Rule COMMA = Terminal(",");
    final Rule DEC = Terminal("--");
    final Rule DIV = Terminal("/", Ch('='));
    final Rule DIVEQU = Terminal("/=");
    final Rule DOT = Terminal(".");
    final Rule ELLIPSIS = Terminal("...");
    final Rule EQU = Terminal("=", Ch('='));
    final Rule EQUAL = Terminal("==");
    final Rule GE = Terminal(">=");
    final Rule GT = Terminal(">", AnyOf("=>"));
    final Rule HAT = Terminal("^", Ch('='));
    final Rule HATEQU = Terminal("^=");
    final Rule INC = Terminal("++");
    final Rule LBRK = Terminal("[");
    final Rule LE = Terminal("<=");
    final Rule LPAR = Terminal("(");
    final Rule LPOINT = Terminal("<");
    final Rule LT = Terminal("<", AnyOf("=<"));
    final Rule LWING = Terminal("{");
    final Rule MINUS = Terminal("-", AnyOf("=-"));
    final Rule MINUSEQU = Terminal("-=");
    final Rule MOD = Terminal("%", Ch('='));
    final Rule MODEQU = Terminal("%=");
    final Rule NOTEQUAL = Terminal("!=");
    final Rule OR = Terminal("|", AnyOf("=|"));
    final Rule OREQU = Terminal("|=");
    final Rule OROR = Terminal("||");
    final Rule PLUS = Terminal("+", AnyOf("=+"));
    final Rule PLUSEQU = Terminal("+=");
    final Rule QUERY = Terminal("?");
    final Rule RBRK = Terminal("]");
    final Rule RPAR = Terminal(")");
    final Rule RPOINT = Terminal(">");
    final Rule RWING = Terminal("}");
    final Rule SEMI = Terminal(";");
    final Rule SL = Terminal("<<", Ch('='));
    final Rule SLEQU = Terminal("<<=");
    final Rule SR = Terminal(">>", AnyOf("=>"));
    final Rule SREQU = Terminal(">>=");
    final Rule STAR = Terminal("*", Ch('='));
    final Rule STAREQU = Terminal("*=");
    final Rule TILDA = Terminal("~");

    //-------------------------------------------------------------------------
    //  helper methods
    //-------------------------------------------------------------------------

    @Override
    protected Rule fromCharLiteral(char c) {
        // turn of creation of parse tree nodes for single characters
        return super.fromCharLiteral(c).suppressNode();
    }

    @SuppressNode
    @DontLabel
    Rule Terminal(String string) {
        return Sequence(string, Spacing()).label('\'' + string + '\'');
    }

    @SuppressNode
    @DontLabel
    Rule Terminal(String string, Rule mustNotFollow) {
        return Sequence(string, TestNot(mustNotFollow), Spacing()).label('\'' + string + '\'');
    }

}
