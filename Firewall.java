import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Firewall {

    private class Packet {
        boolean inbound, tcp;
        int portLowerRange, portUpperRange;
        long ipLowerRange, ipUpperRange;

        private Packet(String direction, String protocol, String port, String address) {
            inbound = direction.equals("inbound");
            tcp = protocol.equals("tcp");
            String[] portRange = port.split("-");
            if (portRange.length == 2) {
                portLowerRange = Integer.parseInt(portRange[0]);
                portUpperRange = Integer.parseInt(portRange[1]);
            } else {
                portLowerRange = Integer.parseInt(port);
                portUpperRange = Integer.parseInt(port);
            }
            String[] ipRange = address.split("-");
            if (ipRange.length == 2) {
                ipLowerRange = addressToLong(ipRange[0]);
                ipUpperRange = addressToLong(ipRange[1]);
            } else {
                ipLowerRange = addressToLong(address);
                ipUpperRange = addressToLong(address);
            }
        }

        private long addressToLong(String address) {
            String[] str = address.split("\\.");
            long result = 0;
            for (int i = 0; i < 4; i++) {
                result += Long.parseLong(str[3 - i]) * (long) Math.pow(256, i);
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Packet)) {
                return false;
            }
            Packet p = (Packet) o;
            return (p.ipLowerRange >= ipLowerRange) && (p.ipUpperRange <= ipUpperRange) &&
                    (p.portLowerRange >= portLowerRange) && (p.portUpperRange <= portUpperRange) &&
                    (inbound == p.inbound) && (tcp == p.tcp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(inbound, tcp, portLowerRange, portUpperRange, ipLowerRange, ipUpperRange);
        }
    }

    public class IntervalST {

        private Node root;

        private class Node {
            long lower, upper, max;
            int size;
            Set<Packet> packetList;
            Node left, right;

            private Node(long lower, long upper, Packet p) {
                this.lower = lower;
                this.upper = upper;
                this.max = upper;
                this.packetList = new HashSet<>();
                this.size = 1;
                this.packetList.add(p);
            }
        }

        public IntervalST(long lower, long upper, Packet p) {
            root = new Node(lower, upper, p);
        }

        public boolean contains(long lower, long upper) {
            return get(lower, upper) != null;
        }

        public Set<Packet> get(long lower, long upper) {
            return get(root, lower, upper);
        }

        private Set<Packet> get(Node x, long lower, long upper) {
            if (x == null)
                return null;
            if (x.lower == lower && x.upper == upper) {
                return x.packetList;
            } else if (lower < x.lower) {
                return get(x.left, lower, upper);
            } else {
                return get(x.right, lower, upper);
            }
        }

        private void update(long lower, long upper, Packet p) {
            get(lower, upper).add(p);
        }

        public void put(long lower, long upper, Packet p) {
            if (contains(lower, upper))
                update(lower, upper, p);
            else
                insert(root, upper, lower, p);
        }

        private Node insert(Node x, long lower, long upper, Packet p) {
            if (x == null) return new Node(lower, upper, p);
            if (lower < x.lower)
                x.left = insert(x.left, lower, upper, p);
            else
                x.right = insert(x.right, lower, upper, p);
            if (x.max < upper)
                x.max = upper;
            x.size++;
            return x;
        }

        public List<Packet> search(long address) {
            List<Packet> result = new ArrayList<>();
            searchHelper(root, address, result);
            return result;
        }

        private void searchHelper(Node n, long address, List<Packet> result) {
            if (n == null)
                return;
            if (address > n.max)
                return;

            if (n.left != null)
                searchHelper(n.left, address, result);

            if (address >= n.lower && address <= n.upper)
                result.addAll(n.packetList);

            if (address < n.lower)
                return;
            if (n.right != null)
                searchHelper(n.right, address, result);
        }
    }

    private IntervalST packetsSortedByAddress;

    public Firewall(String fileName) {
        List<Packet> inputs = new ArrayList<>();
        BufferedReader csvReader;
        try {
            String currentLine;
            csvReader= new BufferedReader(new FileReader(fileName));
            while ((currentLine = csvReader.readLine()) != null) {
                String[] attributes = currentLine.split(",");
                Packet p = new Packet(attributes[0], attributes[1], attributes[2], attributes[3]);
                inputs.add(p);
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int random = (int) (Math.random() * inputs.size());
        Packet current = inputs.get(random);
        inputs.remove(random);
        packetsSortedByAddress = new IntervalST(current.ipLowerRange, current.ipUpperRange, current);
        while (inputs.size() > 0) {
            random = (int) (Math.random() * inputs.size());
            current = inputs.get(random);
            inputs.remove(random);
            packetsSortedByAddress.put(current.ipLowerRange, current.ipUpperRange, current);
        }
    }

    public boolean accept_packet(String direction, String protocol, int port, String address) {
        Packet p = new Packet(direction, protocol, Integer.toString(port), address);
        long a = p.ipLowerRange;
        List<Packet> sameAddress = packetsSortedByAddress.search(a);
        for (Packet input : sameAddress) {
            if (input.equals(p)) {
                return true;
            }
        }
        return false;
    }

    /*Given intervals and a time t, return the intervals that include t */
}
