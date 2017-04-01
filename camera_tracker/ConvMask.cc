
#include <cmath>
#include <vector>
#include <iostream>

#include "ConvMask.hh"

using namespace std;

//----------------------------------------------------------------------
ConvMask::ConvMask(unsigned int plusRadius, unsigned int negativeWidth)
  : m_pRad(plusRadius),
    m_nRad(plusRadius + negativeWidth),
    m_nWidth(negativeWidth),
    m_posNum(0),
    m_negNum(0)

{
  vector<int> _negX;
  vector<int> _negY;
  vector<int> _posX;
  vector<int> _posY;

  double r;

  for (int x = -m_nRad; x <= (int) m_nRad; x++)
    for (int y = -m_nRad; y <= (int) m_nRad; y++)
      {
        r = sqrt(pow((double) x, 2) + pow((double) y, 2));

        // check the smaller of the two radius's
        if (r < m_pRad)
          {
            _posX.push_back(x);
            _posY.push_back(y);
            continue;
          }
        if (r < m_nRad)
          {
            _negX.push_back(x);
            _negY.push_back(y);
            continue;
          }
      }

  m_posNum = _posX.size();
  m_negNum = _negX.size();

  // now build the valarrays

  m_posX.resize(m_posNum);
  m_posY.resize(m_posNum);
  for (int i = 0; i < m_posNum; i++)
    {
      m_posX[i] = _posX[i];
      m_posY[i] = _posY[i];
    }

  m_negX.resize(m_negNum);
  m_negY.resize(m_negNum);
  for (int i = 0; i < m_negNum; i++)
    {
      m_negX[i] = _negX[i];
      m_negY[i] = _negY[i];
    }

  m_regPosX.resize(m_posNum, 0);
  m_regPosY.resize(m_posNum, 0);
  m_regNegX.resize(m_negNum, 0);
  m_regNegY.resize(m_negNum, 0);
}
//----------------------------------------------------------------------
ConvMask::~ConvMask()
{
}
//----------------------------------------------------------------------
void ConvMask::dump() const
{
  cout << "Positive:\n";

  for (size_t i = 0; i < m_posX.size(); i++)
    cout << "(" << m_posX[i] << " x " << m_posY[i] << ")\n";

  cout << "\nNegative:\n";
  for (size_t i = 0; i < m_negX.size(); i++)
    cout << "(" << m_negX[i] << " x " << m_negY[i] << ")\n";
}
