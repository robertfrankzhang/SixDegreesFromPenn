# Read and process nodes.txt
with open('nodes.csv', 'r') as nodes_file:
    nodes = [line.strip().rsplit(',', 1)[0].rsplit('/', 1)[-1].replace('_', ' ') for line in nodes_file]

# Read and process WikiRoughPageRank.txt
with open('WikiRoughPageRank.csv', 'r') as pr_file:
    pr_values = [line.strip().split(',', 1)[1] for line in pr_file]

# Count occurrences of each value in WikiRoughPageRank.txt and print if not equal to 1
pr_count = {}
for value in pr_values:
    if value in nodes:
        pr_count[value] = pr_count.get(value, 0) + 1
    if value not in nodes:
        pr_count[value] = 0

counter = 0
for value, count in pr_count.items():
    if count != 1:
        print(f'{value}: {count}')
        counter += 1
print(counter)

print("DIVIDER")
# Count occurrences of each value in nodes.txt and print if not equal to 1
node_count = {}
for value in nodes:
    if value in pr_values:
        node_count[value] = node_count.get(value, 0) + 1
    if value not in pr_values:
        node_count[value] = 0

counter = 0
for value, count in node_count.items():
    if count != 1:
        print(f'{value}: {count}')
        counter += 1
print(counter)
