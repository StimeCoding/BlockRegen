config.yml FORMAT

    <block_name>:
        time: [int]
        drops:
            <item_name>:
                amount: [int]
                max: [int]
                min: [int]
                chance: [double]
                fortune:
                    amount: [int]
                    max: [int]
                    min: [int]
                    chance: [double]
           <item_name>:
                amount: [int]
                max: [int]
                min: [int]
                chance: [double]
                overwrite: [boolean]
                fortune:
                    amount: [int]
                    max: [int]
                    min: [int]
                    chance: [double]

    Under drops all sections and the drops section are optional, and will be replaced by default values listed.
    The presence of "amount" will overwrite both min and max.
    Default for the drops section are: chance: 1.0, amount: 1 (sets both min and max too if all are absent)
    Default for the fortune section are: chance: 0.0, amount: 0 (sets both min and max too)
    Overwrite makes it replace ALL other possible drops if dropped
