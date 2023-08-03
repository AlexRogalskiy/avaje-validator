package io.avaje.validation.adapter;

import io.avaje.validation.adapter.ValidationContext.AdapterCreateRequest;
import io.avaje.validation.adapter.ValidationContext.Message;

import java.util.Set;

/** Abstract Adapter that validates objects based on Constraint Annotations. */
public abstract class AbstractConstraintAdapter<T> implements ValidationAdapter<T> {

  protected final Message message;
  protected final Set<Class<?>> groups;

  /** Create given the create request */
  protected AbstractConstraintAdapter(AdapterCreateRequest request) {
    this.message = request.message();
    this.groups = request.groups();
  }

  /**
   * Execute Constraint validations for the given object.
   *
   * @param value the object to validate
   * @return false if a violation error should be added
   */
  protected abstract boolean isValid(T value);

  @Override
  public final boolean validate(T value, ValidationRequest req, String propertyName) {
    if (!checkGroups(groups, req)) {
      return true;
    }
    if (!isValid(value)) {
      req.addViolation(message, propertyName);
      return false;
    }
    return true;
  }
}
