package nrs.nrsgui;

/*
 * The range of callbacks made when various happen on a {@link
 * NodeVariable}
 */
interface VariableListener
{
  /** Invoked when the observed variable has had a link added */
  void linkAdded(NodeVariable source);

  /** Invoked when the observed variable has had a link removed */
  void linkRemoved(NodeVariable source);
}
