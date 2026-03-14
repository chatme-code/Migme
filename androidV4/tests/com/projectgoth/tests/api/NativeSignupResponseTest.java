package com.projectgoth.tests.api;

import com.projectgoth.nemesis.listeners.ResendEmailListener;
import com.projectgoth.nemesis.model.MigResponse;
import com.projectgoth.tests.MockitoTestCase;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Exception;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class NativeSignupResponseTest extends MockitoTestCase {

    private static class CustomAnswer implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Answer<Object> answer;

            if (Modifier.isAbstract(invocation.getMethod().getModifiers())) {
                answer = Mockito.RETURNS_DEFAULTS;
            } else {
                answer = Mockito.CALLS_REAL_METHODS;
            }

            return answer.answer(invocation);
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test response when server return old format
     * @throws Exception
     */
    public void testResendVerificationEmailOldResponse() throws Exception {
        MigResponse mockResponse = mock(MigResponse.class);
        when(mockResponse.getResponseData()).thenReturn("ok");
        ResendEmailListener resendEmailListener = mock(ResendEmailListener.class, new CustomAnswer());
        //in order to test protected function, use reflection
        Method method = ResendEmailListener.class.getDeclaredMethod("onResponseReceived", MigResponse.class);
        method.setAccessible(true);
        method.invoke(resendEmailListener, mockResponse);
        ArgumentCaptor<Boolean> arguement = ArgumentCaptor.forClass(Boolean.class);
        verify(resendEmailListener).onEmailResent(arguement.capture());
        assertFalse(arguement.getValue());
    }

    /**
     * Test response when server return new format
     * @throws Exception
     */
    public void testResendVerificationEmailNewResponse() throws Exception {
        MigResponse mockResponse = mock(MigResponse.class);
        when(mockResponse.getResponseData()).thenReturn("{\"value\":true}");
        ResendEmailListener resendEmailListener = mock(ResendEmailListener.class, new CustomAnswer());
        //in order to test protected function, use reflection
        Method method = ResendEmailListener.class.getDeclaredMethod("onResponseReceived", MigResponse.class);
        method.setAccessible(true);
        method.invoke(resendEmailListener, mockResponse);
        ArgumentCaptor<Boolean> arguement = ArgumentCaptor.forClass(Boolean.class);
        verify(resendEmailListener).onEmailResent(arguement.capture());
        assertTrue(arguement.getValue());
    }
}