/*! @license 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011-2012 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/*
 * This script is called from the router "onRequest" trigger, to enforce a central
 * set of authorization rules.
 * 
 * This default implemention simply restricts requests via HTTP to users that are assigned
 * an "openidm-admin" role, and optionally to those that authenticate with TLS mutual
 * authentication (assigned an "openidm-cert" role).
 */

// If true, then allows HTTP requests from "openidm-cert" role.
const allowCert = false;

function contains(a, o) {
    if (typeof(a) != 'undefined' && a != null) {
        for (var i = 0; i <= a.length; i++) {
            if (a[i] === o) {
                return true;
            }
        }
    }
    return false;
}

function isPublicMethodInvocation() {
	logger.info("request.parent.path=" + request.parent.path);
	logger.info("request.parent.method=" + request.parent.method);
	
	if (request.parent.path.match('^/openidm/managed/user') == '/openidm/managed/user') {
		logger.info("Resource==user");
		if (request.parent.method == 'GET') {
			
			logger.info("This is GET request. Selected allowed only. Checking queries.");
			var publicQueries = ['check-userName-availability','for-security-answer','for-credentials', 'get-security-question', 'set-newPassword-for-userName-and-security-answer'];
			if (request.params) {
				var queryName = request.params['_query-id'];
			}
			if (queryName && (publicQueries.indexOf(queryName) > -1)) {
				logger.info("Query " + queryName + " found in the list");
				return true;
			} else {
				logger.info("Query " + queryName + " hasn't been found in a query");
				return false;
			}
			
		} else if (request.parent.method == 'PUT') {
			logger.info("PUT request detected");
			return true;
		} else {
			logger.info("Anonymous POST and DELETE methods are not allowed");
		}
	} else {
		logger.info("Anonymous access not allowed for resources other than user");
		return false;
	}
}

function userIsAuthorized(roles) {
	return contains(roles, 'openidm-authorized');
}

function requestIsAQueryOfName(queryName) {
	return request.params && request.params['_query-id'] && request.params['_query-id'] == queryName;
}

function authorizedUsernameEquals(userName) {
	return request.parent.security['user'] == userName;
}

function requestIsAnActionOfName(actionName) {
	return request.value && request.params && request.params['_action'] && request.params['_action'] == actionName;
}

function requestValueContainsReplaceValueWithKeyOfName(valueKeyName) {
	var key = "replace";
	for (var i = 0; i < request.value.length; i++) {
		if (request.value[i][key] == valueKeyName) {
			return true;
		}
	}
}

function allow() {
    if (typeof(request.parent) === 'undefined' || request.parent.type != 'http') {
        return true;
    }
    var roles = request.parent.security['openidm-roles'];
    if (contains(roles, 'openidm-admin') || contains(roles, 'admin')) {
        return true;
    } else if (allowCert && contains(roles, 'openidm-cert')) {
        return true;
    }else if (requestIsAQueryOfName('for-userName') && userIsAuthorized(roles)){
    	var requestedUserNameDataIdentificator = request.params['uid'];
    	if (authorizedUsernameEquals(requestedUserNameDataIdentificator)) {
    		logger.info("User manipulation with own data");
        	if (requestIsAnActionOfName('patch')) {
        		logger.info("Request is a patch. Checking if trying to change own role");
        		if (requestValueContainsReplaceValueWithKeyOfName('/role')) {
        			logger.info("Trying to change own role is forbidden for standard user");
        			return false;
        		}
        	}
    	} else {
    		logger.info("Manipulation with data of user not equal to logged-in is forbidden");
    		throw "Access denied (Manipulation with data of user not equal to logged-in is forbidden)";
    	}
    	return true;
    } else {
		return isPublicMethodInvocation();
    }
}

if (!allow()) {
    throw "Access denied";
}