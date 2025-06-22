import { Button } from "@/components/ui/button";
import { DialogFooter } from "@/components/ui/dialog";
import {
	Form,
	FormControl,
	FormDescription,
	FormField,
	FormItem,
	FormLabel,
	FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { useStomp } from "@/hooks/use-stomp";
import {
	performatives,
	performativeSchema,
	type Agent,
} from "@/hooks/use-store";
import { zodResolver } from "@hookform/resolvers/zod";
import { Send } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod/v4";

const FormSchema = z.object({
	aid: z.string(),
	performative: performativeSchema,
	conversationId: z.string(),
	message: z.string(),
});

const AgentMessageForm = ({ agent }: { agent: Agent }) => {
	const { publish } = useStomp();

	const form = useForm<z.infer<typeof FormSchema>>({
		resolver: zodResolver(FormSchema),
		defaultValues: {
			aid: agent.aid,
			performative: "INFORM",
			conversationId: "",
			message: "",
		},
	});

	function onSubmit(data: z.infer<typeof FormSchema>) {
		console.log("Sent message to agent", data);
		// sendMessage("/app/agent-message", data);
		publish("/app/agent-message", data);
		form.reset();
	}
	return (
		<Form {...form}>
			<form
				onSubmit={form.handleSubmit(onSubmit)}
				className="flex flex-col gap-6 mt-4"
			>
				<FormField
					control={form.control}
					name="performative"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Performative</FormLabel>
							<FormDescription>
								Select the performative of the message
							</FormDescription>
							<Select
								onValueChange={field.onChange}
								defaultValue={field.value}
							>
								<FormControl>
									<SelectTrigger>
										<SelectValue placeholder="Select a verified email to display" />
									</SelectTrigger>
								</FormControl>
								<SelectContent>
									{performatives.map((performative) => (
										<SelectItem
											key={performative}
											value={performative}
										>
											{performative}
										</SelectItem>
									))}
								</SelectContent>
							</Select>
							<FormMessage />
						</FormItem>
					)}
				/>
				<FormField
					control={form.control}
					name="conversationId"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Conversation ID</FormLabel>
							<FormControl>
								<Input
									placeholder="eg. power-relief, trigger"
									{...field}
								/>
							</FormControl>
							<FormMessage />
						</FormItem>
					)}
				/>
				<FormField
					control={form.control}
					name="message"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Message</FormLabel>
							<FormControl>
								<Input
									placeholder="Message content (optional)"
									{...field}
								/>
							</FormControl>
							<FormMessage />
						</FormItem>
					)}
				/>
				<DialogFooter>
					<Button type="submit" className="flex items-center gap-2">
						<Send />
						Send
					</Button>
				</DialogFooter>
			</form>
		</Form>
	);
};
export default AgentMessageForm;
