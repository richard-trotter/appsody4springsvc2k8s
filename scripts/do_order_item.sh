#
# This script will exercise the inventory service sample, with the following sequence of 
# REST requests:
#
#    1. GET an inventory item
#    2. POST a request to triggger simulation of an order request notification
#    3. GET the indicated inventory item a second time, to verify decrease of inventory stock

if [ $# != 1 ]
then
  echo "USAGE: $(basename $0) <hostport>"
  exit
fi

LOCATION=$1
ITEM_ID=13401

LIVENESS_ENDPOINT=/demo/actuator/liveness

cat << EOF
* $(basename $0)
*
* Running 'liveness' probe: ${LIVENESS_ENDPOINT}
EOF
curl -w "\nSTATUS: %{http_code}\n" http://${LOCATION}${LIVENESS_ENDPOINT}

cat << EOF

* Running inventory order sequence using inventory service location: ${LOCATION}
* At end of sequence, verify "stock" level has decreased by one.

EOF

echo "### GET item"
curl -w "\nSTATUS: %{http_code}\n" http://${LOCATION}/demo/inventory/item/${ITEM_ID}
echo ""
echo "### POST item order simulation"
curl -w "STATUS: %{http_code}\n" -X POST "http://${LOCATION}/demo/inventory/order?itemId=${ITEM_ID}&count=1"
echo ""
sleep 2
echo "### GET item"
curl -w "\nSTATUS: %{http_code}\n" http://${LOCATION}/demo/inventory/item/${ITEM_ID}
echo ""

