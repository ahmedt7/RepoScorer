/**
 * Repository scoring service.
 *
 * <p>The application exposes an HTTP API for searching GitHub repositories,
 * delegates the search to GitHub through a Feign client, and enriches the
 * returned repositories with a deterministic popularity score.</p>
 */
package org.redCare.api.service.repoScorer;
