package net.bytebuddy.implementation.bytecode.member;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.test.utility.MockitoRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class MethodVariableAccessTest {

    private final TypeDescription typeDescription;

    private final int readCode, writeCode;

    private final int size;

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private MethodVisitor methodVisitor;

    @Mock
    private Implementation.Context implementationContext;

    public MethodVariableAccessTest(Class<?> type, int readCode, int writeCode, int size) {
        this.typeDescription = mock(TypeDescription.class);
        when(typeDescription.isPrimitive()).thenReturn(type.isPrimitive());
        when(typeDescription.represents(type)).thenReturn(true);
        this.readCode = readCode;
        this.writeCode = writeCode;
        this.size = size;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Object.class, Opcodes.ALOAD, Opcodes.ASTORE, 1},
                {boolean.class, Opcodes.ILOAD, Opcodes.ISTORE, 1},
                {byte.class, Opcodes.ILOAD, Opcodes.ISTORE, 1},
                {short.class, Opcodes.ILOAD, Opcodes.ISTORE, 1},
                {char.class, Opcodes.ILOAD, Opcodes.ISTORE, 1},
                {int.class, Opcodes.ILOAD, Opcodes.ISTORE, 1},
                {long.class, Opcodes.LLOAD, Opcodes.LSTORE, 2},
                {float.class, Opcodes.FLOAD, Opcodes.FSTORE, 1},
                {double.class, Opcodes.DLOAD, Opcodes.DSTORE, 2},
        });
    }

    @After
    public void tearDown() throws Exception {
        verifyZeroInteractions(implementationContext);
    }

    @Test
    public void testLoading() throws Exception {
        StackManipulation stackManipulation = MethodVariableAccess.of(typeDescription).loadFrom(4);
        assertThat(stackManipulation.isValid(), is(true));
        StackManipulation.Size size = stackManipulation.apply(methodVisitor, implementationContext);
        assertThat(size.getSizeImpact(), is(this.size));
        assertThat(size.getMaximalSize(), is(this.size));
        verify(methodVisitor).visitVarInsn(readCode, 4);
        verifyNoMoreInteractions(methodVisitor);
    }

    @Test
    public void testStoring() throws Exception {
        StackManipulation stackManipulation = MethodVariableAccess.of(typeDescription).storeAt(4);
        assertThat(stackManipulation.isValid(), is(true));
        StackManipulation.Size size = stackManipulation.apply(methodVisitor, implementationContext);
        assertThat(size.getSizeImpact(), is(this.size));
        assertThat(size.getMaximalSize(), is(this.size));
        verify(methodVisitor).visitVarInsn(writeCode, 4);
        verifyNoMoreInteractions(methodVisitor);
    }
}