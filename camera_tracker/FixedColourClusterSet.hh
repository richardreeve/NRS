#ifndef _FIXEDCOLOURCLUSTERSET_HH_
#define _FIXEDCOLOURCLUSTERSET_HH_


/**
 * A programmatic method for hardcoding a the numerical parameters
 * associated with a colour cluster set.
 */
class FixedColourClusterSet
{
public:
  FixedColourClusterSet();

  double* covariance_inverse(int colour);

  double mean(int colour, int channel);

  int numbers(int i);

  const char* labels(int i);

  int count() const;

private:
  int m_count;
};

#endif
