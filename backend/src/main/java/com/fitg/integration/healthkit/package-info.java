/**
 * Receives batches pushed by the iOS app (HealthKit data never leaves the phone otherwise).
 * Maps DTOs -> Activity / DailyMetric and upserts idempotently.
 */
package com.fitg.integration.healthkit;
