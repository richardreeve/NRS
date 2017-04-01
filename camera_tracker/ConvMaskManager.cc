
#include "ConvMaskManager.hh"
#include "ConvMask.hh"

using namespace std;

//----------------------------------------------------------------------
ConvMaskManager::ConvMaskManager()
{
}
//----------------------------------------------------------------------
ConvMaskManager::~ConvMaskManager()
{
  for (vector_list::iterator i = m_masks.begin(); i != m_masks.end(); i++)
    delete *i;
}
//----------------------------------------------------------------------
ConvMask& ConvMaskManager::getMask(unsigned int posRad, 
                                   unsigned int negWidth)
{
  for (vector_list::iterator i = m_masks.begin(); i != m_masks.end(); i++)
    if (((*i)->getPosRadius() == posRad) 
        and ((*i)->getNegWidth() == negWidth))
      return *(*i);
  
  // ...otherwise if not found, make a new object, add to list, return it
  m_masks.push_back(new ConvMask(posRad, negWidth));
  return *(m_masks.back());
}
