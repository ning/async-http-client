package com.ning.http.client;

/**
 * Interface that allows injecting signature calculator into
 * {@link RequestBuilder} so that signature calculation and inclusion can
 * be added as a pluggable component.
 * 
 * @since 1.1
 */
public interface SignatureCalculator
{
    /**
     * Method called when {@link RequestBuilder#build} method is called.
     * Should first calculate signature information and then modify request
     * (using passed {@link RequestBuilder}) to add signature (usually as
     * an HTTP header).
     * 
     * @param requestBuilder builder that can be used to modify request, usually
     *   by adding header that includes calculated signature. Be sure NOT to
     *   call {@link RequestBuilder#build} since this will cause infinite recursion
     * @param request Request that is being built; needed to access content to
     *   be signed
     */
    public void calculateAndAddSignature(String url, Request request,
            RequestBuilderBase<?> requestBuilder);
}
