/**
 * <p>This package contains utility classes for working with REST API's. This functionality 
 *    is based on the 3rd-party Jersey; this library is not shipped with SSC so it will need 
 *    to be provided with the bug tracker implementation.</p>
 * <p>Bug tracker implementations can either extend one of these utility classes, or instantiate
 *    one of these classes as a utility object. The latter may be useful if the bug tracker
 *    implementation supports different methods for connecting to the bug tracker. One notable
 *    example is if the implementation supports multiple authentication methods, like username/password
 *    and token-based authentication. Depending on the configured mechanism, the bug tracker 
 *    implementation can then instantiate either a {@link com.fortify.util.rest.AuthenticatingRestConnection}
 *    or a token-based equivalent.</p>
 */
package com.fortify.util.rest;
