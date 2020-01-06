# coding-challenge

One of the major problems that I ran into was figuring out how to deal with the ranges
of ports and ip addresses. I initially tried to just put everything into a set using a
hashcode but this wouldnt work due to the nature of the ranges. For example I couldn't
make a hashcode that outputs the same thing for 1-10 as 5. 

I then tried to at least get it working by just putting all inputs into a list and then 
just testing one by one until one of them matches, or return false if none matches. Although
this worked it would be inefficient to search through every item, resulting in a O(n)
runtime for accept_packet. 

My next and final revision was insert the ranges of the ip address into a interval tree,
with the idea of searching through the tree to find all ip address ranges that fit the
input argument ip address in log(n) time. With the much smaller list of all matching ip
ranges I would then just iterate through the packets and see if any match all four arguments.
To do this, I first converted the ip address into a long lower bound range and a upper 
bound range. These ranges would then be inserted into the tree randomly to insure a "bushy"
tree in nlog(n) time. This worked well and reduced runtime for accept_packet but made the
constructor take longer.

If I had more time to revise my code I would probably make the interval tree a self balancing
tree just to make sure that all search operations take at most log(n) time. I would also 
have improved on my current design by including the range of ports as well instead of just ip 
address
