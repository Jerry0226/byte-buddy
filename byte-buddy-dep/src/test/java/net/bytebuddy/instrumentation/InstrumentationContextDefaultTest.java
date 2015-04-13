package net.bytebuddy.instrumentation;

import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeWriter;
import net.bytebuddy.instrumentation.field.FieldDescription;
import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.method.ParameterList;
import net.bytebuddy.instrumentation.method.bytecode.ByteCodeAppender;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackManipulation;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackSize;
import net.bytebuddy.instrumentation.type.InstrumentedType;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.instrumentation.type.TypeList;
import net.bytebuddy.instrumentation.type.auxiliary.AuxiliaryType;
import net.bytebuddy.test.utility.MockitoRule;
import net.bytebuddy.test.utility.MoreOpcodes;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

public class InstrumentationContextDefaultTest {

    private static final String FOO = "foo", BAR = "bar", QUX = "qux", BAZ = "baz";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private TypeDescription instrumentedType, firstDescription, secondDescription;

    @Mock
    private InstrumentedType.TypeInitializer typeInitializer, otherTypeInitializer, thirdTypeInitializer;

    @Mock
    private ClassFileVersion classFileVersion;

    @Mock
    private ClassVisitor classVisitor;

    @Mock
    private MethodVisitor methodVisitor;

    @Mock
    private FieldVisitor fieldVisitor;

    @Mock
    private TypeWriter.MethodPool methodPool;

    @Mock
    private TypeWriter.MethodPool.Entry entry, otherEntry;

    @Mock
    private Instrumentation.Context.ExtractableView.InjectedCode injectedCode;

    @Mock
    private AuxiliaryType auxiliaryType, otherAuxiliaryType;

    @Mock
    private DynamicType firstDynamicType, secondDynamicType;

    @Mock
    private TypeDescription firstFieldType, secondFieldType;

    @Mock
    private StackManipulation firstFieldValue, secondFieldValue, injectedCodeAppender, terminationAppender;

    @Mock
    private Instrumentation.SpecialMethodInvocation firstSpecialInvocation, secondSpecialInvocation;

    @Mock
    private MethodDescription firstSpecialMethod, secondSpecialMethod;

    @Mock
    private AuxiliaryType.NamingStrategy auxiliaryTypeNamingStrategy;

    @Mock
    private TypeDescription firstSpecialType, secondSpecialType, firstSpecialReturnType, secondSpecialReturnType,
            firstSpecialParameterType, secondSpecialParameterType, firstSpecialExceptionType, secondSpecialExceptionType;

    @Mock
    private FieldDescription firstField, secondField;

    @Mock
    private TypeDescription firstFieldDeclaringType, secondFieldDeclaringType;

    private TypeList firstSpecialExceptionTypes, secondSpecialExceptionTypes;

    @Before
    public void setUp() throws Exception {
        firstSpecialExceptionTypes = new TypeList.Explicit(Collections.singletonList(firstSpecialExceptionType));
        secondSpecialExceptionTypes = new TypeList.Explicit(Collections.singletonList(secondSpecialExceptionType));
        when(instrumentedType.getInternalName()).thenReturn(BAZ);
        when(methodPool.target(any(MethodDescription.class))).thenReturn(entry);
        when(auxiliaryType.make(any(String.class), any(ClassFileVersion.class), any(AuxiliaryType.MethodAccessorFactory.class)))
                .thenReturn(firstDynamicType);
        when(firstDynamicType.getTypeDescription()).thenReturn(firstDescription);
        when(otherAuxiliaryType.make(any(String.class), any(ClassFileVersion.class), any(AuxiliaryType.MethodAccessorFactory.class)))
                .thenReturn(secondDynamicType);
        when(secondDynamicType.getTypeDescription()).thenReturn(secondDescription);
        when(classVisitor.visitMethod(any(int.class), any(String.class), any(String.class), any(String.class), any(String[].class)))
                .thenReturn(methodVisitor);
        when(classVisitor.visitField(any(int.class), any(String.class), any(String.class), any(String.class), any(Object.class)))
                .thenReturn(fieldVisitor);
        when(firstFieldValue.apply(any(MethodVisitor.class), any(Instrumentation.Context.class))).thenReturn(new StackManipulation.Size(0, 0));
        when(secondFieldValue.apply(any(MethodVisitor.class), any(Instrumentation.Context.class))).thenReturn(new StackManipulation.Size(0, 0));
        when(firstFieldType.getStackSize()).thenReturn(StackSize.ZERO);
        when(firstFieldType.getDescriptor()).thenReturn(BAR);
        when(secondFieldType.getStackSize()).thenReturn(StackSize.ZERO);
        when(secondFieldType.getDescriptor()).thenReturn(QUX);
        when(injectedCode.getStackManipulation()).thenReturn(injectedCodeAppender);
        when(injectedCodeAppender.apply(any(MethodVisitor.class), any(Instrumentation.Context.class)))
                .thenReturn(new StackManipulation.Size(0, 0));
        when(terminationAppender.apply(any(MethodVisitor.class), any(Instrumentation.Context.class)))
                .thenReturn(new StackManipulation.Size(0, 0));
        when(firstSpecialInvocation.getMethodDescription()).thenReturn(firstSpecialMethod);
        when(firstSpecialInvocation.getTypeDescription()).thenReturn(firstSpecialType);
        when(firstSpecialMethod.getReturnType()).thenReturn(firstSpecialReturnType);
        when(firstSpecialMethod.getInternalName()).thenReturn(FOO);
        when(firstSpecialMethod.getExceptionTypes()).thenReturn(firstSpecialExceptionTypes);
        when(firstSpecialParameterType.getDescriptor()).thenReturn(BAZ);
        when(firstSpecialReturnType.getDescriptor()).thenReturn(QUX);
        when(firstSpecialExceptionType.getInternalName()).thenReturn(FOO);
        when(firstSpecialParameterType.getStackSize()).thenReturn(StackSize.ZERO);
        when(firstSpecialReturnType.getStackSize()).thenReturn(StackSize.ZERO);
        when(firstSpecialInvocation.apply(any(MethodVisitor.class), any(Instrumentation.Context.class))).thenReturn(new StackManipulation.Size(0, 0));
        ParameterList firstSpecialMethodParameters = ParameterList.Explicit.latent(firstSpecialMethod, Collections.singletonList(firstSpecialParameterType));
        when(firstSpecialMethod.getParameters()).thenReturn(firstSpecialMethodParameters);
        when(secondSpecialInvocation.getMethodDescription()).thenReturn(secondSpecialMethod);
        when(secondSpecialInvocation.getTypeDescription()).thenReturn(secondSpecialType);
        when(secondSpecialMethod.getInternalName()).thenReturn(BAR);
        when(secondSpecialMethod.getReturnType()).thenReturn(secondSpecialReturnType);
        when(secondSpecialMethod.getExceptionTypes()).thenReturn(secondSpecialExceptionTypes);
        when(secondSpecialParameterType.getDescriptor()).thenReturn(BAR);
        when(secondSpecialReturnType.getDescriptor()).thenReturn(FOO);
        when(secondSpecialExceptionType.getInternalName()).thenReturn(BAZ);
        when(secondSpecialParameterType.getStackSize()).thenReturn(StackSize.ZERO);
        when(secondSpecialReturnType.getStackSize()).thenReturn(StackSize.ZERO);
        when(secondSpecialInvocation.apply(any(MethodVisitor.class), any(Instrumentation.Context.class))).thenReturn(new StackManipulation.Size(0, 0));
        ParameterList secondSpecialMethodParameters = ParameterList.Explicit.latent(secondSpecialMethod, Collections.singletonList(secondSpecialParameterType));
        when(secondSpecialMethod.getParameters()).thenReturn(secondSpecialMethodParameters);
        when(firstField.getFieldType()).thenReturn(firstFieldType);
        when(firstField.getName()).thenReturn(FOO);
        when(firstField.getInternalName()).thenReturn(FOO);
        when(firstField.getDescriptor()).thenReturn(BAR);
        when(firstField.getDeclaringType()).thenReturn(firstFieldDeclaringType);
        when(firstFieldDeclaringType.getInternalName()).thenReturn(QUX);
        when(secondField.getFieldType()).thenReturn(secondFieldType);
        when(secondField.getName()).thenReturn(BAR);
        when(secondField.getInternalName()).thenReturn(BAR);
        when(secondField.getDescriptor()).thenReturn(FOO);
        when(secondField.getDeclaringType()).thenReturn(secondFieldDeclaringType);
        when(secondFieldDeclaringType.getInternalName()).thenReturn(BAZ);
    }

    @Test
    public void testInitialContextIsEmpty() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().size(), is(0));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verifyZeroInteractions(classVisitor);
        verify(methodPool).target(MethodDescription.Latent.typeInitializerOf(instrumentedType));
        verifyNoMoreInteractions(methodPool);
        verify(injectedCode).isDefined();
        verifyNoMoreInteractions(injectedCode);
    }

    @Test
    public void testAuxiliaryTypeRegistration() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().size(), is(0));
        assertThat(instrumentationContext.register(auxiliaryType), is(firstDescription));
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().size(), is(1));
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().contains(firstDynamicType), is(true));
        assertThat(instrumentationContext.register(otherAuxiliaryType), is(secondDescription));
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().size(), is(2));
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().contains(firstDynamicType), is(true));
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().contains(secondDynamicType), is(true));
        assertThat(instrumentationContext.register(auxiliaryType), is(firstDescription));
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().size(), is(2));
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().contains(firstDynamicType), is(true));
        assertThat(instrumentationContext.getRegisteredAuxiliaryTypes().contains(secondDynamicType), is(true));
    }

    @Test
    public void testDrainEmpty() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verifyZeroInteractions(classVisitor);
    }

    @Test
    public void testDrainNoUserCodeNoInjectedCodeNoTypeInitializer() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verifyZeroInteractions(classVisitor);
        verify(typeInitializer).isDefined();
        verifyNoMoreInteractions(typeInitializer);
        verify(injectedCode).isDefined();
        verifyNoMoreInteractions(injectedCode);
    }

    @Test
    public void testDrainUserCodeNoInjectedCodeNoTypeInitializer() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.IMPLEMENT);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(entry).getSort();
        verify(entry).apply(classVisitor, instrumentationContext, MethodDescription.Latent.typeInitializerOf(instrumentedType));
        verifyNoMoreInteractions(entry);
        verifyZeroInteractions(classVisitor);
        verify(typeInitializer, atLeast(1)).isDefined();
        verifyNoMoreInteractions(typeInitializer);
        verify(injectedCode, atLeast(1)).isDefined();
        verifyNoMoreInteractions(injectedCode);
    }

    @Test
    public void testDrainNoUserCodeInjectedCodeNoTypeInitializer() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        when(injectedCode.isDefined()).thenReturn(true);
        when(otherTypeInitializer.isDefined()).thenReturn(true);
        when(typeInitializer.expandWith(injectedCodeAppender)).thenReturn(otherTypeInitializer);
        when(otherTypeInitializer.terminate()).thenReturn(terminationAppender);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(entry).getSort();
        verify(typeInitializer).expandWith(injectedCodeAppender);
        verifyNoMoreInteractions(typeInitializer);
        verify(injectedCode, atLeast(1)).isDefined();
        verify(injectedCode).getStackManipulation();
        verifyNoMoreInteractions(injectedCode);
        verify(otherTypeInitializer, atLeast(1)).isDefined();
        verify(otherTypeInitializer).terminate();
        verifyNoMoreInteractions(otherTypeInitializer);
        verify(terminationAppender).apply(methodVisitor, instrumentationContext);
        verifyNoMoreInteractions(terminationAppender);
    }

    @Test
    public void testDrainNoUserCodeNoInjectedCodeTypeInitializer() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        when(typeInitializer.isDefined()).thenReturn(true);
        when(typeInitializer.terminate()).thenReturn(terminationAppender);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(entry).getSort();
        verifyNoMoreInteractions(entry);
        verify(typeInitializer, atLeast(1)).isDefined();
        verify(typeInitializer).terminate();
        verifyNoMoreInteractions(typeInitializer);
        verify(injectedCode, atLeast(1)).isDefined();
        verifyNoMoreInteractions(injectedCode);
        verify(terminationAppender).apply(methodVisitor, instrumentationContext);
        verifyNoMoreInteractions(terminationAppender);
    }

    @Test
    public void testDrainUserCodeNoInjectedCodeTypeInitializer() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.IMPLEMENT);
        when(typeInitializer.isDefined()).thenReturn(true);
        when(entry.prepend(new ByteCodeAppender.Simple(typeInitializer))).thenReturn(otherEntry);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(entry).getSort();
        verify(entry).prepend(new ByteCodeAppender.Simple(typeInitializer));
        verifyNoMoreInteractions(entry);
        verify(otherEntry).apply(classVisitor, instrumentationContext, MethodDescription.Latent.typeInitializerOf(instrumentedType));
        verify(typeInitializer, atLeast(1)).isDefined();
        verifyNoMoreInteractions(typeInitializer);
        verify(injectedCode, atLeast(1)).isDefined();
        verifyNoMoreInteractions(injectedCode);
    }

    @Test
    public void testDrainFieldCacheEntries() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        FieldDescription firstField = instrumentationContext.cache(firstFieldValue, firstFieldType);
        assertThat(instrumentationContext.cache(firstFieldValue, firstFieldType), is(firstField));
        FieldDescription secondField = instrumentationContext.cache(secondFieldValue, secondFieldType);
        assertThat(instrumentationContext.cache(secondFieldValue, secondFieldType), is(secondField));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        when(typeInitializer.expandWith(any(StackManipulation.class))).thenReturn(otherTypeInitializer);
        when(otherTypeInitializer.expandWith(any(StackManipulation.class))).thenReturn(thirdTypeInitializer);
        when(thirdTypeInitializer.terminate()).thenReturn(terminationAppender);
        when(thirdTypeInitializer.isDefined()).thenReturn(true);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitField(eq(Instrumentation.Context.ExtractableView.FIELD_CACHE_MODIFIER),
                Mockito.startsWith(Instrumentation.Context.Default.FIELD_CACHE_PREFIX),
                eq(BAR),
                Mockito.isNull(String.class),
                Mockito.isNull(Object.class));
        verify(classVisitor).visitField(eq(Instrumentation.Context.ExtractableView.FIELD_CACHE_MODIFIER),
                Mockito.startsWith(Instrumentation.Context.Default.FIELD_CACHE_PREFIX),
                eq(QUX),
                Mockito.isNull(String.class),
                Mockito.isNull(Object.class));
        verify(typeInitializer).expandWith(any(StackManipulation.class));
        verify(otherTypeInitializer).expandWith(any(StackManipulation.class));
        verify(thirdTypeInitializer).terminate();
        verify(thirdTypeInitializer).isDefined();
        verify(terminationAppender).apply(methodVisitor, instrumentationContext);
        verifyNoMoreInteractions(terminationAppender);
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotRegisterFieldAfterDraining() throws Exception {
        Instrumentation.Context.ExtractableView instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verifyZeroInteractions(classVisitor);
        verify(methodPool).target(MethodDescription.Latent.typeInitializerOf(instrumentedType));
        verifyNoMoreInteractions(methodPool);
        verify(injectedCode).isDefined();
        verifyNoMoreInteractions(injectedCode);
        instrumentationContext.cache(firstFieldValue, firstFieldType);
    }

    @Test
    public void testAccessorMethodRegistration() throws Exception {
        Instrumentation.Context.Default instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        MethodDescription firstMethodDescription = instrumentationContext.registerAccessorFor(firstSpecialInvocation);
        assertThat(firstMethodDescription.getParameters(), is(ParameterList.Explicit.latent(firstMethodDescription, Collections.singletonList(firstSpecialParameterType))));
        assertThat(firstMethodDescription.getReturnType(), is(firstSpecialReturnType));
        assertThat(firstMethodDescription.getInternalName(), startsWith(FOO));
        assertThat(firstMethodDescription.getModifiers(), is(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER));
        assertThat(firstMethodDescription.getExceptionTypes(), is(firstSpecialExceptionTypes));
        assertThat(instrumentationContext.registerAccessorFor(firstSpecialInvocation), is(firstMethodDescription));
        when(secondSpecialMethod.isStatic()).thenReturn(true);
        MethodDescription secondMethodDescription = instrumentationContext.registerAccessorFor(secondSpecialInvocation);
        assertThat(secondMethodDescription.getParameters(), is(ParameterList.Explicit.latent(secondMethodDescription, Collections.singletonList(secondSpecialParameterType))));
        assertThat(secondMethodDescription.getReturnType(), is(secondSpecialReturnType));
        assertThat(secondMethodDescription.getInternalName(), startsWith(BAR));
        assertThat(secondMethodDescription.getModifiers(), is(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER | Opcodes.ACC_STATIC));
        assertThat(secondMethodDescription.getExceptionTypes(), is(secondSpecialExceptionTypes));
        assertThat(instrumentationContext.registerAccessorFor(firstSpecialInvocation), is(firstMethodDescription));
        assertThat(instrumentationContext.registerAccessorFor(secondSpecialInvocation), is(secondMethodDescription));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER), Matchers.startsWith(FOO),
                eq("(" + BAZ + ")" + QUX), isNull(String.class), aryEq(new String[]{FOO}));
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER | Opcodes.ACC_STATIC), Matchers.startsWith(BAR),
                eq("(" + BAR + ")" + FOO), isNull(String.class), aryEq(new String[]{BAZ}));
    }

    @Test
    public void testAccessorMethodRegistrationWritesFirst() throws Exception {
        Instrumentation.Context.Default instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        MethodDescription firstMethodDescription = instrumentationContext.registerAccessorFor(firstSpecialInvocation);
        assertThat(instrumentationContext.registerAccessorFor(firstSpecialInvocation), is(firstMethodDescription));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER), Matchers.startsWith(FOO),
                eq("(" + BAZ + ")" + QUX), isNull(String.class), aryEq(new String[]{FOO}));
        verify(methodVisitor).visitCode();
        verify(methodVisitor).visitInsn(MoreOpcodes.ALOAD_0);
        verify(methodVisitor).visitInsn(MoreOpcodes.ALOAD_1);
        verify(firstSpecialInvocation).apply(methodVisitor, instrumentationContext);
        verify(methodVisitor).visitInsn(Opcodes.ARETURN);
        verify(methodVisitor).visitMaxs(2, 1);
        verify(methodVisitor).visitEnd();
    }

    @Test
    public void testAccessorMethodRegistrationWritesSecond() throws Exception {
        when(secondSpecialMethod.isStatic()).thenReturn(true);
        Instrumentation.Context.Default instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        MethodDescription secondMethodDescription = instrumentationContext.registerAccessorFor(secondSpecialInvocation);
        assertThat(instrumentationContext.registerAccessorFor(secondSpecialInvocation), is(secondMethodDescription));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER | Opcodes.ACC_STATIC), Matchers.startsWith(BAR),
                eq("(" + BAR + ")" + FOO), isNull(String.class), aryEq(new String[]{BAZ}));
        verify(methodVisitor).visitCode();
        verify(methodVisitor).visitInsn(MoreOpcodes.ALOAD_0);
        verify(secondSpecialInvocation).apply(methodVisitor, instrumentationContext);
        verify(methodVisitor).visitInsn(Opcodes.ARETURN);
        verify(methodVisitor).visitMaxs(1, 0);
        verify(methodVisitor).visitEnd();
    }

    @Test
    public void testFieldGetterRegistration() throws Exception {
        Instrumentation.Context.Default instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        MethodDescription firstFieldGetter = instrumentationContext.registerGetterFor(firstField);
        assertThat(firstFieldGetter.getParameters(), is((ParameterList) new ParameterList.Empty()));
        assertThat(firstFieldGetter.getReturnType(), is(firstFieldType));
        assertThat(firstFieldGetter.getInternalName(), startsWith(FOO));
        assertThat(firstFieldGetter.getModifiers(), is(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER));
        assertThat(firstFieldGetter.getExceptionTypes(), is((TypeList) new TypeList.Empty()));
        assertThat(instrumentationContext.registerGetterFor(firstField), is(firstFieldGetter));
        when(secondField.isStatic()).thenReturn(true);
        MethodDescription secondFieldGetter = instrumentationContext.registerGetterFor(secondField);
        assertThat(secondFieldGetter.getParameters(), is((ParameterList) new ParameterList.Empty()));
        assertThat(secondFieldGetter.getReturnType(), is(secondFieldType));
        assertThat(secondFieldGetter.getInternalName(), startsWith(BAR));
        assertThat(secondFieldGetter.getModifiers(), is(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER | Opcodes.ACC_STATIC));
        assertThat(secondFieldGetter.getExceptionTypes(), is((TypeList) new TypeList.Empty()));
        assertThat(instrumentationContext.registerGetterFor(firstField), is(firstFieldGetter));
        assertThat(instrumentationContext.registerGetterFor(secondField), is(secondFieldGetter));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER), Matchers.startsWith(FOO),
                eq("()" + BAR), isNull(String.class), isNull(String[].class));
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER | Opcodes.ACC_STATIC), Matchers.startsWith(BAR),
                eq("()" + QUX), isNull(String.class), isNull(String[].class));
    }

    @Test
    public void testFieldGetterRegistrationWritesFirst() throws Exception {
        Instrumentation.Context.Default instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        MethodDescription firstMethodDescription = instrumentationContext.registerGetterFor(firstField);
        assertThat(instrumentationContext.registerGetterFor(firstField), is(firstMethodDescription));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER), Matchers.startsWith(FOO),
                eq("()" + BAR), isNull(String.class), isNull(String[].class));
        verify(methodVisitor).visitCode();
        verify(methodVisitor).visitInsn(MoreOpcodes.ALOAD_0);
        verify(methodVisitor).visitFieldInsn(Opcodes.GETFIELD, QUX, FOO, BAR);
        verify(methodVisitor).visitInsn(Opcodes.ARETURN);
        verify(methodVisitor).visitMaxs(1, 1);
        verify(methodVisitor).visitEnd();
    }

    @Test
    public void testFieldGetterRegistrationWritesSecond() throws Exception {
        when(secondField.isStatic()).thenReturn(true);
        Instrumentation.Context.Default instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        MethodDescription secondMethodDescription = instrumentationContext.registerGetterFor(secondField);
        assertThat(instrumentationContext.registerGetterFor(secondField), is(secondMethodDescription));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER | Opcodes.ACC_STATIC), Matchers.startsWith(BAR),
                eq("()" + QUX), isNull(String.class), isNull(String[].class));
        verify(methodVisitor).visitCode();
        verify(methodVisitor).visitFieldInsn(Opcodes.GETSTATIC, BAZ, BAR, FOO);
        verify(methodVisitor).visitInsn(Opcodes.ARETURN);
        verify(methodVisitor).visitMaxs(0, 0);
        verify(methodVisitor).visitEnd();
    }

    @Test
    public void testFieldSetterRegistration() throws Exception {
        Instrumentation.Context.Default instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        MethodDescription firstFieldSetter = instrumentationContext.registerSetterFor(firstField);
        assertThat(firstFieldSetter.getParameters(), is(ParameterList.Explicit.latent(firstFieldSetter, Collections.singletonList(firstFieldType))));
        assertThat(firstFieldSetter.getReturnType(), is((TypeDescription) new TypeDescription.ForLoadedType(void.class)));
        assertThat(firstFieldSetter.getInternalName(), startsWith(FOO));
        assertThat(firstFieldSetter.getModifiers(), is(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER));
        assertThat(firstFieldSetter.getExceptionTypes(), is((TypeList) new TypeList.Empty()));
        assertThat(instrumentationContext.registerSetterFor(firstField), is(firstFieldSetter));
        when(secondField.isStatic()).thenReturn(true);
        MethodDescription secondFieldSetter = instrumentationContext.registerSetterFor(secondField);
        assertThat(secondFieldSetter.getParameters(), is(ParameterList.Explicit.latent(secondFieldSetter, Collections.singletonList(secondFieldType))));
        assertThat(secondFieldSetter.getReturnType(), is((TypeDescription) new TypeDescription.ForLoadedType(void.class)));
        assertThat(secondFieldSetter.getInternalName(), startsWith(BAR));
        assertThat(secondFieldSetter.getModifiers(), is(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER | Opcodes.ACC_STATIC));
        assertThat(secondFieldSetter.getExceptionTypes(), is((TypeList) new TypeList.Empty()));
        assertThat(instrumentationContext.registerSetterFor(firstField), is(firstFieldSetter));
        assertThat(instrumentationContext.registerSetterFor(secondField), is(secondFieldSetter));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER), Matchers.startsWith(FOO),
                eq("(" + BAR + ")V"), isNull(String.class), isNull(String[].class));
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER | Opcodes.ACC_STATIC), Matchers.startsWith(BAR),
                eq("(" + QUX + ")V"), isNull(String.class), isNull(String[].class));
    }

    @Test
    public void testFieldSetterRegistrationWritesFirst() throws Exception {
        Instrumentation.Context.Default instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        MethodDescription firstMethodDescription = instrumentationContext.registerSetterFor(firstField);
        assertThat(instrumentationContext.registerSetterFor(firstField), is(firstMethodDescription));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER), Matchers.startsWith(FOO),
                eq("(" + BAR + ")V"), isNull(String.class), isNull(String[].class));
        verify(methodVisitor).visitCode();
        verify(methodVisitor).visitInsn(MoreOpcodes.ALOAD_0);
        verify(methodVisitor).visitInsn(MoreOpcodes.ALOAD_1);
        verify(methodVisitor).visitFieldInsn(Opcodes.PUTFIELD, QUX, FOO, BAR);
        verify(methodVisitor).visitInsn(Opcodes.RETURN);
        verify(methodVisitor).visitMaxs(2, 1);
        verify(methodVisitor).visitEnd();
    }

    @Test
    public void testFieldSetterRegistrationWritesSecond() throws Exception {
        when(secondField.isStatic()).thenReturn(true);
        Instrumentation.Context.Default instrumentationContext = new Instrumentation.Context.Default(instrumentedType,
                auxiliaryTypeNamingStrategy,
                typeInitializer,
                classFileVersion);
        MethodDescription secondMethodDescription = instrumentationContext.registerSetterFor(secondField);
        assertThat(instrumentationContext.registerSetterFor(secondField), is(secondMethodDescription));
        when(entry.getSort()).thenReturn(TypeWriter.MethodPool.Entry.Sort.SKIP);
        instrumentationContext.drain(classVisitor, methodPool, injectedCode);
        verify(classVisitor).visitMethod(eq(AuxiliaryType.MethodAccessorFactory.ACCESSOR_METHOD_MODIFIER | Opcodes.ACC_STATIC), Matchers.startsWith(BAR),
                eq("(" + QUX + ")V"), isNull(String.class), isNull(String[].class));
        verify(methodVisitor).visitCode();
        verify(methodVisitor).visitInsn(MoreOpcodes.ALOAD_0);
        verify(methodVisitor).visitFieldInsn(Opcodes.PUTSTATIC, BAZ, BAR, FOO);
        verify(methodVisitor).visitInsn(Opcodes.RETURN);
        verify(methodVisitor).visitMaxs(1, 0);
        verify(methodVisitor).visitEnd();
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(Instrumentation.Context.Default.class).applyMutable();
        ObjectPropertyAssertion.of(Instrumentation.Context.Default.FieldCacheEntry.class).apply();
        ObjectPropertyAssertion.of(Instrumentation.Context.Default.AccessorMethodDelegation.class).apply();
        ObjectPropertyAssertion.of(Instrumentation.Context.Default.FieldSetter.class).apply();
        ObjectPropertyAssertion.of(Instrumentation.Context.Default.FieldGetter.class).apply();
    }
}