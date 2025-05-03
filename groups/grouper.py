import json
import argparse

# Set up argument parsing
parser = argparse.ArgumentParser(description="Grouper.")
parser.add_argument('input_file', type=str, help='The input JSON file with group data.')
parser.add_argument('max_weight', type=int, help='Max weight of item')
parser.add_argument('-o', '--output_file', type=str, default='output.json', help='The output JSON file (default: output.json).')
args = parser.parse_args()

# Open and load the input JSON file
with open(args.input_file) as f:
    groups = json.load(f)

output = {}

# Process the data and calculate the output
for group in groups:
    total: int = group["total"]
    items: list[str] = group["items"]
    k = total / len(items)
    if k > args.max_weight:
        raise ValueError(f"Max weight exceeded: {k} > {args.max_weight}")
    for item in items:
        output[item] = k

max_weight_generated = max(output.values())
multiplier = args.max_weight // max_weight_generated
print(multiplier)
for key in output:
    output[key] *= multiplier

# Write the output to the specified file
with open(args.output_file, "w+") as f:
    json.dump(output, f, indent=4)
