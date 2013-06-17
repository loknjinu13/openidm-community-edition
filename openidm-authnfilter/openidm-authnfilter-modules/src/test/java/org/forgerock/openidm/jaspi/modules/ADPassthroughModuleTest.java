/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2013 ForgeRock Inc.
 */

package org.forgerock.openidm.jaspi.modules;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class ADPassthroughModuleTest {

    private ADPassthroughModule adPassthroughModule;

    private ADPassthroughAuthenticator adPassthroughAuthenticator;

    @BeforeMethod
    public void setUp() {

        adPassthroughAuthenticator = mock(ADPassthroughAuthenticator.class);

        adPassthroughModule = new ADPassthroughModule(adPassthroughAuthenticator);
    }

    @Test
    public void shouldValidateRequestWhenUsernameHeaderIsNull() {

        //Given
        MessageInfo messageInfo = mock(MessageInfo.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();
        AuthData authData = mock(AuthData.class);

        HttpServletRequest request = mock(HttpServletRequest.class);

        given(messageInfo.getRequestMessage()).willReturn(request);
        given(request.getHeader("X-OpenIDM-Username")).willReturn(null);
        given(request.getHeader("X-OpenIDM-Password")).willReturn("PASSWORD");

        //When
        AuthStatus authStatus = adPassthroughModule.validateRequest(messageInfo, clientSubject, serviceSubject,
                authData);

        //Then
        verifyZeroInteractions(adPassthroughAuthenticator);
        verifyZeroInteractions(authData);
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test
    public void shouldValidateRequestWhenUsernameHeaderIsEmptyString() {

        //Given
        MessageInfo messageInfo = mock(MessageInfo.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();
        AuthData authData = mock(AuthData.class);

        HttpServletRequest request = mock(HttpServletRequest.class);

        given(messageInfo.getRequestMessage()).willReturn(request);
        given(request.getHeader("X-OpenIDM-Username")).willReturn("");
        given(request.getHeader("X-OpenIDM-Password")).willReturn("PASSWORD");

        //When
        AuthStatus authStatus = adPassthroughModule.validateRequest(messageInfo, clientSubject, serviceSubject,
                authData);

        //Then
        verifyZeroInteractions(adPassthroughAuthenticator);
        verifyZeroInteractions(authData);
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test
    public void shouldValidateRequestWhenPasswordHeaderIsNull() {

        //Given
        MessageInfo messageInfo = mock(MessageInfo.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();
        AuthData authData = mock(AuthData.class);

        HttpServletRequest request = mock(HttpServletRequest.class);

        given(messageInfo.getRequestMessage()).willReturn(request);
        given(request.getHeader("X-OpenIDM-Username")).willReturn("USERNAME");
        given(request.getHeader("X-OpenIDM-Password")).willReturn(null);

        //When
        AuthStatus authStatus = adPassthroughModule.validateRequest(messageInfo, clientSubject, serviceSubject,
                authData);

        //Then
        verifyZeroInteractions(adPassthroughAuthenticator);
        verifyZeroInteractions(authData);
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test
    public void shouldValidateRequestWhenPasswordHeaderIsEmptyString() {

        //Given
        MessageInfo messageInfo = mock(MessageInfo.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();
        AuthData authData = mock(AuthData.class);

        HttpServletRequest request = mock(HttpServletRequest.class);

        given(messageInfo.getRequestMessage()).willReturn(request);
        given(request.getHeader("X-OpenIDM-Username")).willReturn("USERNAME");
        given(request.getHeader("X-OpenIDM-Password")).willReturn("");

        //When
        AuthStatus authStatus = adPassthroughModule.validateRequest(messageInfo, clientSubject, serviceSubject,
                authData);

        //Then
        verifyZeroInteractions(adPassthroughAuthenticator);
        verifyZeroInteractions(authData);
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test
    public void shouldValidateRequestWhenAuthenticationSuccessful() throws AuthException {

        //Given
        MessageInfo messageInfo = mock(MessageInfo.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();
        AuthData authData = mock(AuthData.class);

        HttpServletRequest request = mock(HttpServletRequest.class);

        given(messageInfo.getRequestMessage()).willReturn(request);
        given(request.getHeader("X-OpenIDM-Username")).willReturn("USERNAME");
        given(request.getHeader("X-OpenIDM-Password")).willReturn("PASSWORD");

        given(adPassthroughAuthenticator.authenticate(authData, "PASSWORD")).willReturn(true);

        //When
        AuthStatus authStatus = adPassthroughModule.validateRequest(messageInfo, clientSubject, serviceSubject, authData);

        //Then
        verify(authData).setUsername("USERNAME");
        assertEquals(authStatus, AuthStatus.SUCCESS);
    }

    @Test
    public void shouldValidateRequestWhenAuthenticationFailed() throws AuthException {

        //Given
        MessageInfo messageInfo = mock(MessageInfo.class);
        Subject clientSubject = new Subject();
        Subject serviceSubject = new Subject();
        AuthData authData = mock(AuthData.class);

        HttpServletRequest request = mock(HttpServletRequest.class);

        given(messageInfo.getRequestMessage()).willReturn(request);
        given(request.getHeader("X-OpenIDM-Username")).willReturn("USERNAME");
        given(request.getHeader("X-OpenIDM-Password")).willReturn("PASSWORD");

        given(adPassthroughAuthenticator.authenticate(authData, "PASSWORD")).willReturn(false);

        //When
        AuthStatus authStatus = adPassthroughModule.validateRequest(messageInfo, clientSubject, serviceSubject, authData);

        //Then
        verify(authData).setUsername("USERNAME");
        assertEquals(authStatus, AuthStatus.SEND_FAILURE);
    }

    @Test
    public void shouldSecureResponse() throws AuthException {

        //Given
        MessageInfo messageInfo = mock(MessageInfo.class);
        Subject serviceSubject = new Subject();

        //When
        AuthStatus authStatus = adPassthroughModule.secureResponse(messageInfo, serviceSubject);

        //Then
        assertEquals(authStatus, AuthStatus.SEND_SUCCESS);
        verifyZeroInteractions(messageInfo);
    }
}
