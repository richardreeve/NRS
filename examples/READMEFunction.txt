This example net attempts to evolve the value of x 
which gives the maximum value for the function 
y = 5 - (x-1)^2 (i.e. 1), or the minimum value of the 
function y = 2(x-2)^2 + 3 (i.e. 2). The function is 
entered in the calcFitness method of the FunctionCalcNode
class of the ga. To change between these 2 functions, 
comment out one, and uncomment the other in the method. 
Components NRS.gui,NRS.control, NRS.calculation and 
NRS.ga must be running before the network is loaded. 
Click the StartGA box to start. The gene value is 
connected to the display, and also to a float delay node 
with a delay of 400ms, which then returns the value to the 
evaluation variable. This is because the ga is designed
to be connected to the nsim, which would return an 
evaluation of the genes to the evaluation variable after a 
small delay. The delay node is added between the gene value 
and the evaluation variable to avoid the disruption to the 
ordering of messages which occurs if they are connected 
directly to each other. As there is only one gene in this 
network, the crossover rate is 0, and the mutation rate is 
used to evolve the value. For the integer network this rate
is set to 20%, and for the float network to 30%. These 
values can be altered in the .net file to see how this affect
the end result. As negative values disrupt the selection
process, the max gene value for the first function 
(y = 5 - (x-1)^2) is 10, as this is what is handled in the 
fitness function to rule out negative numbers.

FunctionIntData.net and FunctionFloatData.net are the same as
FunctionInt.net and FunctionFloat.net, except they use the 
DataLogger to write each gene value to a file. These require
NRS.datalogger to be run. 